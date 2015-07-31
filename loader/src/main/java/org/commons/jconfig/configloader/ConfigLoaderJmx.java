package org.commons.jconfig.configloader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.commons.jconfig.config.ConfigException;
import org.commons.jconfig.config.ConfigLoaderAdapterID;
import org.commons.jconfig.internal.jmx.ConfigManagerJmx;
import org.commons.jconfig.internal.jmx.ConfigManagerJvm;
import org.commons.jconfig.internal.jmx.LoadAppConfigsNotification;
import org.commons.jconfig.internal.jmx.LoadModuleConfigsNotification;
import org.commons.jconfig.internal.jmx.VirtualMachineException;
import org.commons.jconfig.loader.adapters.Adapter;
import org.commons.jconfig.loader.adapters.AutoConf;
import org.commons.jconfig.loader.adapters.AutoConfAdapter;
import org.commons.jconfig.loader.adapters.LsgAdapter;

/**
 * Implements @ConfigLoaderMXBean and Broadcasts two Notifications<br><br>
 * 
 * 1. @LoadAppConfigsNotification<br>
 * 2. @LoadModuleConfigsNotification<br>
 * 
 * <b>Note</b> Modular ability to load from multiple @ConfigSource.<Br>
 * currently supports @AutoConf
 */
public class ConfigLoaderJmx
extends NotificationBroadcasterSupport implements ConfigLoaderJmxMXBean {

    private final Logger logger = Logger.getLogger(ConfigLoaderJmx.class);
    private ConfigLoaderConfig config;
    private static final String SETS_TYPE = "_Sets_Type_";
    private static final String SETS = "_Sets_";
    private static final String SETS_KEY_NODE = "key";
    private static final String SETS_KEYLIST_NODE = "keyList";

    /**
     * Empty constructor for registering MBean, since without registering Loader
     * MBean we cannot get ConfigLoaderConfig instance.
     */
    protected ConfigLoaderJmx() {

    }

    /**
     * @param config
     *            ConfigLoaderConfig instance.
     */
    public ConfigLoaderJmx(ConfigLoaderConfig config) {
        init(config);
    }

    /**
     * Load up list of known Adapters currently
     * 
     * @param config
     * @AutoConfConfigModuleAdapter
     * @LSGConfigModuleAdapter
     */
    protected void init(final ConfigLoaderConfig config) {
        this.config = config;
        AutoConf autoconf = new AutoConf(config);
        AutoConfAdapter stdrdAdapter = new AutoConfAdapter(autoconf);
        LsgAdapter lsgAdapter = new LsgAdapter(autoconf);

        adapterMap.put(stdrdAdapter.getUri(), stdrdAdapter);
        adapterMap.put(lsgAdapter.getUri(), lsgAdapter);
    }

    /**
     * Notifications broadcast by ConfigLoader
     */
    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {

        MBeanNotificationInfo loadAppInfo = new MBeanNotificationInfo(
                new String[] { LoadAppConfigsNotification.APP_CONFIGS_TYPE},
                LoadAppConfigsNotification.class.getName(), LoadAppConfigsNotification.APP_CONFIGS_DESC);

        MBeanNotificationInfo LoadModuleInfo = new MBeanNotificationInfo(
                new String[] { LoadModuleConfigsNotification.MODULE_CONFIGS_TYPE},
                LoadModuleConfigsNotification.class.getName(), LoadModuleConfigsNotification.MODULE_CONFIGS_DESC);

        return new MBeanNotificationInfo[] {loadAppInfo, LoadModuleInfo};
    }

    @Override
    public void subscribeConfigs(final String appName) throws ConfigException {
    }

    /**
     * for the given appName, set the MBeans with configurations<br>
     * <br>
     * <b>Note</b> : Config MBean without a corresponding module in the
     * configuration source is not an error case. Only a warning is logged. it
     * is the caller's responsibility to verify all modules are populated.<br>
     * 
     * @param managerObjectName
     * @param force
     *            if false, only set if the configurations changed from previous
     *            load. else, load regardless.
     * @throws ConfigException
     *             if appName not found in the configurations source.
     */
    public void loadAppConfigs(final ObjectName managerObjectName, final boolean force) throws VirtualMachineException,
    ConfigException {
        String applicationName = managerObjectName.getKeyProperty(ConfigManagerJvm.APPNAME_KEY);
        // Configuration Manager MBean of the application to load configs for
        ConfigManagerJvm vm = new ConfigManagerJvm(managerObjectName);

        //the result of the load operation
        boolean result = false;
        boolean sendNotification = true;
        String notificationMsg = "config loading for " + applicationName + " application is complete";

        try {

            vm.attach();
            MBeanServerConnection mbsc = vm.getJMXConnector().getMBeanServerConnection();

            // Query MBean in the Config MBeans domain
            //
            Set<ObjectName> configNames = new TreeSet<ObjectName>(mbsc.queryNames(new ObjectName(
                    ConfigManagerJmx.CONFIG_MBEANS_SEARCH_PATTERN + applicationName + ",*"), null));
            if ( configNames.size() == 0 ) {
                logger.error("No configuration MBeans registered in " + applicationName);
                return;
            }

            JsonNode appNode = getApplicationConfig(mbsc, applicationName, configNames);
            logger.debug("loading " + applicationName + " with configs:  " + appNode.toString());

            // For each registered configuration MBean set the MBean properties
            for (ObjectName bname : configNames) {

                try {
                    loadModuleConfigs(mbsc, applicationName, appNode, bname, force);
                } catch (ConfigException e) {
                    //Swallow the exception.  No need to fail the entire app just cause one module is bad.
                    logger.error("no configuration found for the " + moduleName(bname) + " module for the "
                            + applicationName + " applicaton");
                }
            }

            result = true;

        } catch (VirtualMachineException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigException(e);
        } finally {
            if (sendNotification) {
                // Send the notification for this appName
                LoadAppConfigsNotification n = new LoadAppConfigsNotification(this, sequenceNumber++,
                        System.currentTimeMillis(), notificationMsg, applicationName, result);
                sendNotification(n);
                logger.info("Notified application " + applicationName + " about new configs.");
            }
            try {
                vm.close();
            } catch (VirtualMachineException e) {
                logger.error(e.getMessage());
            }
        }
    }

    /**
     * Generate hash value for application config and return 0 if config not
     * found or any error returned.
     * 
     * @param vm
     *            {@link ConfigManagerJvm} Assume vm is already attached
     * @param appName
     *            Application name
     * @return hash value for applicationConfig value
     */
    public Integer getAppConfigHash(ConfigManagerJvm vm, String appName) {
        try {
            MBeanServerConnection mbsc = vm.getJMXConnector().getMBeanServerConnection();
            Set<ObjectName> configNames = new TreeSet<ObjectName>();            
            configNames = new TreeSet<ObjectName>(mbsc.queryNames(new ObjectName(
                    ConfigManagerJmx.CONFIG_MBEANS_SEARCH_PATTERN + appName + ",*"), null));

            if (configNames.size() == 0) {
                return 0;
            }

            JsonNode appNode = getApplicationConfig(mbsc, appName, configNames);
            if (appNode != null) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.writeValueAsString(appNode).hashCode();
            } else {
                return 0;
            }
        } catch (ConfigException e) {
            logger.error("Error getting app config hash ", e);
        } catch (MalformedObjectNameException e) {
            logger.error("Error getting app config hash ", e);
        } catch (IOException e) {
            logger.error("Error getting app config hash ", e);
        }
        return 0;
    }
    
    
    /**
     * for the Config MBean in the given appName, set the MBean with
     * configurations<br>
     * <br>
     * <b>Note</b> : Config MBean attributes without a corresponding value in
     * the configuration source is<br>
     * not an error case. Only a warning is logged. it is the caller's
     * responsibility to verify all attributes<br>
     * are populated.<br>
     * 
     * @param mbsc
     * @param appName
     * @param appNode
     * @param bname
     * @param force
     *            if false, only set if the configurations changed from previous
     *            load. else, load regardless.
     * @throws ConfigException
     * @throws ConfigException
     *             if configuration module is missing in the JsonNode
     * @throws IOException
     * @throws ReflectionException
     * @throws MBeanException
     * @throws InvalidAttributeValueException
     * @throws AttributeNotFoundException
     * @throws InstanceNotFoundException
     * @throws IntrospectionException
     */
    private void loadModuleConfigs(final MBeanServerConnection mbsc, final String appName, final JsonNode appNode,
            final ObjectName bname, final boolean force)
            throws ConfigException, InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException, IOException, IntrospectionException {

        boolean result = false;
        boolean sendNotification = true;
        String module = moduleName(bname);
        String notificationMsg = "config loading for module " + module + " of " + appName + " application is complete";
        try {
            if ( ! hasModule(appNode, module) ) {
                notificationMsg = "no configuration found for the " + module + " module for the " + appName + " applicaton";
                throw new ConfigException(notificationMsg);
            }
            JsonNode moduleNode = getModule(appNode, module);

            // If we are reSynching, check if the config source configuration changed from our last load
            Integer checkSum = moduleNode.hashCode();
            if ( ! force && checkSum.equals(moduleConfCheckSumMap.get(appName + "." + module)) ) {
                logger.debug("configuration for the " + module + " module for the " + appName + " applicaton still in synch");
                sendNotification = false;
                return;
            }
            logger.info("configuration for the " + module + " module for the " + appName + " applicaton: "
                    + moduleNode.toString());

            // update our chucksum for this module
            moduleConfCheckSumMap.put(appName + "." + module, checkSum);

            // Set the configuration MBeans attributes in the jmx get the attributes for this MBean
            MBeanAttributeInfo[] attribs = mbsc.getMBeanInfo(bname).getAttributes();

            // Populate the MBean attributes using the config node
            for (MBeanAttributeInfo attrib : attribs) {

                if (!attrib.isWritable()) {
                    logger.info(module + " " + attrib.getName() + " MBean attribute is read only");
                    continue;
                }

                if (!hasAttribute(moduleNode, attrib.getName())) {
                    logger.info(module + " " + attrib.getName() + " MBean attribute is missing configuration entry");
                    continue;
                }

                try {
                    mbsc.setAttribute(bname, createConfigAttribute(attrib, moduleNode));
                } catch (ConfigException e) {
                    // Swallow the exception. No need to fail the entire MBean
                    // just cause one attribute is bad.
                    logger.info(module + " " + attrib.getName() + " MBean attribute load error");
                }
            }

            result = true;
        } finally {

            if ( sendNotification ) {
                //
                // Send the notification for this module
                LoadModuleConfigsNotification n =
                        new LoadModuleConfigsNotification(
                                this, sequenceNumber++, System.currentTimeMillis(),
                                notificationMsg, appName, module,  result);

                sendNotification(n);
                logger.info("Notified module " + module + " of application " + appName + " about new configs.");
            }
        }
    }

    /**
     * Get the application config Json node for the given given app. null, if
     * does not exist.<br>
     * <br>
     * 
     * builds a Json structure of all the module configs for the given<br>
     * application.
     * 
     * @param appName
     * @param configNames
     *            - list of MBean names to set
     * @param config
     * @return
     * @throws ConfigException
     * @throws ConfigException
     */
    private JsonNode getApplicationConfig(final MBeanServerConnection mbsc, final String appName,
            final Set<ObjectName> configNames) throws ConfigException {

        String moduleName = null;
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode appNode = mapper.createObjectNode();

        for (ObjectName bname : configNames) {

            moduleName = moduleName(bname);

            Object strAdapter = null;

            // Check if this config class needs to use a config module adapter
            try {
                strAdapter = mbsc.getAttribute(bname, "ConfigLoaderAdapter");
            } catch (AttributeNotFoundException e) {
                logger.error("LoaderAdapter attribute missing for " + moduleName + " Config MBean", e);
            } catch (InstanceNotFoundException e) {
                logger.error("LoaderAdapter attribute missing for " + moduleName + " Config MBean", e);
            } catch (MBeanException e) {
                logger.error("LoaderAdapter attribute missing for " + moduleName + " Config MBean", e);
            } catch (ReflectionException e) {
                logger.error("LoaderAdapter attribute missing for " + moduleName + " Config MBean", e);
            } catch (IOException e) {
                logger.error("LoaderAdapter attribute missing for " + moduleName + " Config MBean", e);
            }

            // No adapter specified, log it
            // and assume we are using standard autoConf config module
            if ( strAdapter == null ) {
                strAdapter = ConfigLoaderAdapterID.JSON_AUTOCONF.getUri();
            }
            if (strAdapter.equals(ConfigLoaderAdapterID.LSG_AUTOCONF.getUri())) {
                System.out.println("");
            }
            // Check to see if we have an adapter for this config module
            Adapter adapter = adapterMap.get(strAdapter);
            if (adapter != null) {
                JsonNode moduleNode = adapter.getModuleNode(appName, moduleName);
                appNode.put(moduleName, moduleNode);
            } else {
                throw new ConfigException("Failed to create adapter.");
            }
        }
        return appNode;
    }

    /**
     * Existence of module config Json node for the given module in for the given app.
     * 
     * @param appNode
     * @param module
     * @return
     */
    private static boolean hasModule(final JsonNode appNode, final String module) {
        return appNode.has(module);
    }

    /**
     * Get the module config Json node for the given module in for the given app.
     * null, if does not exist.
     * 
     * @param appNode
     * @param module
     * @return
     */
    private static JsonNode getModule(final JsonNode appNode, final String module) {
        return appNode.get(module);
    }

    /**
     * Existence of attribute node node for the given attribute in the given module.
     * includes checks for attributes inside "Sets"
     * 
     * @param moduleNode
     * @param attribName
     * @return
     */
    private static boolean hasAttribute(final JsonNode moduleNode, final String attribName) {
        /* check in default values */
        if (moduleNode.has(attribName)) {
            return true;
        }
        if (moduleNode.has(SETS)) {
            JsonNode sets = moduleNode.get(SETS);
            if (sets.isArray()) {
                for (JsonNode node : sets) {
                    if (!node.path(SETS_KEYLIST_NODE).path(attribName).isMissingNode()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Given and attributeInfo and a configuration node, create and return a set
     * attribute object
     * 
     * @param attrib
     *            MBean attribute
     * @param node
     *            configuration node
     * 
     * @return a populated attribute
     * @throws ConfigException
     *             if configuration entry is missing in the JsonNode<br>
     *             Or type mismatch between entry in JsonNode and attribute
     *             type.
     */
    private static Attribute createConfigAttribute(final MBeanAttributeInfo attrib, final JsonNode node) throws ConfigException {
        String attribName = attrib.getName();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        ArrayNode newSets = mapper.createArrayNode();

        if (null != node.get(attribName)) {
            rootNode.put(attribName, node.get(attribName));
        }

        if (null != node.get(SETS_TYPE)) {
            rootNode.put(SETS_TYPE, node.get(SETS_TYPE));
            JsonNode origSets = node.get(SETS);
            if ((origSets != null) && origSets.isArray()) {
                Iterator<JsonNode> itr = origSets.iterator();
                while (itr.hasNext()) {
                    JsonNode setElement = itr.next();
                    JsonNode key = setElement.get(SETS_KEY_NODE);
                    JsonNode keyList = setElement.get(SETS_KEYLIST_NODE);

                    // bad element; skip it
                    if ((key == null) || (keyList == null)) {
                        continue;
                    }

                    Iterator<String> keyListItr = keyList.getFieldNames();
                    while (keyListItr.hasNext()) {
                        String keyListAttribName = keyListItr.next();

                        // Is this the attribute name we are interested in
                        if (keyListAttribName.equals(attribName)) {

                            // Build a new node with just the keyList attribute
                            // we
                            // are interested in
                            ObjectNode newNode = mapper.createObjectNode();
                            ObjectNode attributeNode = mapper.createObjectNode();
                            attributeNode.put(attribName, keyList.path(attribName));
                            newNode.put(SETS_KEY_NODE, key);
                            newNode.put(SETS_KEYLIST_NODE, attributeNode);

                            newSets.add(newNode);
                        }
                    }

                }
            }
            rootNode.put(SETS, newSets);
        }

        return new Attribute(attrib.getName(), rootNode.toString());
    }

    /**
     * given MBean ObjectName, return simple string name
     * 
     * @param bname
     * @return
     */
    private static String moduleName(final ObjectName bname) {
        return bname.getKeyProperty("type");
    }

    /**
     * Notification sequence counter
     */
    private long sequenceNumber = 1;

    /**
     * Map of "Application.module" JsonNode references.  This cache used to test for
     * configuration changes on reSycn operations
     */
    private final Map<String, Integer> moduleConfCheckSumMap = new HashMap<String, Integer>();

    /**
     * Map of known config module adapters
     */
    private final Map<String, Adapter> adapterMap = new HashMap<String, Adapter>();

    public ConfigLoaderConfig getConfig() {
        return config;
    }

}
