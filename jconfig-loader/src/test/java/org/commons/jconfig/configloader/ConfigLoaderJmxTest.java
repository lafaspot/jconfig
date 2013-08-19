package org.commons.jconfig.configloader;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import mockit.Deencapsulation;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.commons.jconfig.config.ConfigException;
import org.commons.jconfig.configloader.ConfigLoaderConfig;
import org.commons.jconfig.configloader.ConfigLoaderJmx;
import org.commons.jconfig.datatype.TimeValue;
import org.commons.jconfig.internal.ConfigAdapterJson;
import org.commons.jconfig.internal.jmx.ConfigManagerJmx;
import org.commons.jconfig.internal.jmx.ConfigManagerJvm;
import org.commons.jconfig.internal.jmx.LoadAppConfigsNotification;
import org.commons.jconfig.internal.jmx.LoadModuleConfigsNotification;
import org.commons.jconfig.internal.jmx.VirtualMachineException;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.gson.JsonParseException;

public class ConfigLoaderJmxTest {

    private MBeanServer mbs = null;
    private SherpaConfig sherpaMbean = null;
    private FilerGateConfig filergateMbean = null;
    private PimCacheConfig pimcacheMBean = null;
    private LSGConfig lsgMbean = null;
    private ObjectName sherpaMbeanName = null;
    private ObjectName filergateMbeanName = null;
    private ObjectName pimcacheMbeanName = null;
    private ObjectName lsgMBeanName = null;
    private ObjectName ConfgMgrAppMbeanName = null;

    private ConfigLoaderJmx cl;

    @BeforeTest
    public void setup() throws Exception {

        ConfigLoaderConfig config = new ConfigLoaderConfig();
        // AutoConf test file
        String filename = new File(".").getCanonicalPath() + "/src/test/resources/autoconf.json";
        config.setConfigFileName(filename);
        config.setConfigSyncInterval(TimeValue.parse("60m"));
        // Check for file changes every 1 sec
        config.setConfigSyncInterval(new TimeValue(1, TimeUnit.SECONDS));


        cl = new ConfigLoaderJmx();
        cl.init(config);

        mbs = ManagementFactory.getPlatformMBeanServer();

        //
        // Register this testApp as an Imap ConfigManager Managed App
        ConfgMgrAppMbeanName = new ObjectName("com.xyz.configmgr:type=ConfigManager,appName=Imap");
        mbs.registerMBean(new ConfigManagerJmx("Imap"), ConfgMgrAppMbeanName);

        //
        // Register a config mbean (SherpaConfig)
        sherpaMbean = new SherpaConfig();
        sherpaMbeanName = new ObjectName(ConfigManagerJmx.CONFIG_MBEANS_DOMAIN_PREFIX + "SherpaConfig,appName=Imap");
        mbs.registerMBean(sherpaMbean, sherpaMbeanName);

        //
        // Register a config mbean (FilerGateConfig)
        filergateMbean = new FilerGateConfig();
        filergateMbeanName = new ObjectName(ConfigManagerJmx.CONFIG_MBEANS_DOMAIN_PREFIX
                + "FilerGateConfig,appName=Imap");
        mbs.registerMBean(filergateMbean, filergateMbeanName);

        //
        // Register a config mbean (PimCacheConfig)
        pimcacheMBean = new PimCacheConfig();
        pimcacheMbeanName = new ObjectName(ConfigManagerJmx.CONFIG_MBEANS_DOMAIN_PREFIX + "PimCacheConfig,appName=Imap");
        mbs.registerMBean(pimcacheMBean, pimcacheMbeanName);

        //
        // Register a config mbean (LsgConfig)
        lsgMbean = new LSGConfig();
        lsgMBeanName = new ObjectName(ConfigManagerJmx.CONFIG_MBEANS_DOMAIN_PREFIX + "LsgConfig,appName=Imap");
        mbs.registerMBean(lsgMbean, lsgMBeanName);
    }

    @Test(groups = { "loader" })
    public void getNotificationInfoTest() {

        MBeanNotificationInfo[] infos = cl.getNotificationInfo();

        //Two notifications only
        Assert.assertEquals(infos.length, 2);

        //App notification
        Assert.assertEquals(infos[0].getNotifTypes()[0], LoadAppConfigsNotification.APP_CONFIGS_TYPE);
        Assert.assertEquals(infos[0].getName(), LoadAppConfigsNotification.class.getName());

        //Module notification
        Assert.assertEquals(infos[1].getNotifTypes()[0], LoadModuleConfigsNotification.MODULE_CONFIGS_TYPE);
        Assert.assertEquals(infos[1].getName(), LoadModuleConfigsNotification.class.getName());
    }

    @Test(groups = { "loader" })
    public void moduleNameTest() throws Exception {

        ObjectName good = new ObjectName(ConfigManagerJmx.CONFIG_MBEANS_DOMAIN_PREFIX + "good");
        Assert.assertEquals(Deencapsulation.invoke(cl, "moduleName", good), "good");
    }

    /**
     * Validate below json 
        { "_Sets_" : [ { "key" : [ "323" ],
                "keyList" : { "FilerGateServerName" : "fg323.mail.vip.mud.xyz.com" }
              },
              { "key" : [ "318" ],
                "keyList" : { "FilerGateServerName" : "fg318.mail.vip.mud.xyz.com" }
              },
              { "key" : [ "9323" ],
                "keyList" : { "FilerGateServerName" : "fg9323.mail.vip.ne1.xyz.com" }
              },
              { "key" : [ "9323" ],
                "keyList" : { "FilerGateServerName" : "fg9323.mail.vip.ne1.xyz.com" }
              }
            ],
          "_Sets_Type_" : "FARM",
          "FilerGateServerName" : "fg9323.mail.vip.ne1.xyz.com"
        }
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    @Test(groups = { "loader" })
    public void createConfigAttributeTest() throws JsonParseException, JsonMappingException, IOException {
        MBeanAttributeInfo attribInfo = new MBeanAttributeInfo("FilerGateServerName", "java.lang.String", "some description", true, true, false, null);
        
        String filename = "./src/test/resources/autoconf_nolsgclient.json";
        File f = new File(filename);
        ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
        mapper.configure( DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonNode configNode = mapper.readValue(f, JsonNode.class);
        
        JsonNode filerConfig = configNode.get("qmda").get("FilerGateConfig");
        Attribute attrib =  Deencapsulation.invoke(cl, "createConfigAttribute", attribInfo, filerConfig);
        
        Assert.assertEquals(attrib.getValue(), "{\"_Sets_Type_\":\"FARM\",\"_Sets_\":[{\"key\":[\"323\"],\"keyList\":{\"FilerGateServerName\":\"fg323.mail.vip.mud.xyz.com\"}},{\"key\":[\"318\"],\"keyList\":{\"FilerGateServerName\":\"fg318.mail.vip.mud.xyz.com\"}},{\"key\":[\"9323\"],\"keyList\":{\"FilerGateServerName\":\"fg9323.mail.vip.ne1.xyz.com\"}}]}");
        
        JsonNode sherpaConfig = configNode.get("Modules").get("SherpaConfig");
        MBeanAttributeInfo attribInfo1 = new MBeanAttributeInfo("Timeout", "java.lang.String", "some description", true, true, false, null);
        attrib =  Deencapsulation.invoke(cl, "createConfigAttribute", attribInfo1, sherpaConfig);
        System.out.println(attrib.getValue());
        Assert.assertEquals(attrib.getValue(), "{\"Timeout\":\"300ms\"}");
    }

    @Test(groups = { "loader" })
    public void loadModuleConfigsTest() throws Exception {
        ConfigManagerJvm vm = new ConfigManagerJvm(new ObjectName("com.xyz.configmgr:type=ConfigManager,appName=Imap"));

        // get an MBean Server Connection
        vm.attach();
        MBeanServerConnection mbsc = vm.getJMXConnector().getMBeanServerConnection();
        
        //Test part1 - load module which is defined in application node
        Set<ObjectName> pimCacheObjectName = new TreeSet<ObjectName>();
        pimCacheObjectName.add(new ObjectName("com.xyz.configs:type=PimCacheConfig"));
        JsonNode imapNode = Deencapsulation.invoke(cl, "getApplicationConfig", mbsc, "Imap", pimCacheObjectName);

        // Set the PimCache MBean values
        Deencapsulation.invoke(cl, "loadModuleConfigs", mbsc, "Imap", imapNode, pimcacheMbeanName, true);
        // Now verify that the attributes were correctly populated
        Assert.assertEquals(pimcacheMBean.getPimEmdRecvTimeout(), "{\"PimEmdRecvTimeout\":\"4ms\"}");
        Assert.assertEquals(pimcacheMBean.getPimEmdSendTimeout(), "{\"PimEmdSendTimeout\":\"3ms\"}");
        Assert.assertEquals(pimcacheMBean.getPimLmaRecvTimeout(), "{\"PimLmaRecvTimeout\":\"2ms\"}");
        Assert.assertEquals(pimcacheMBean.getPimLmaSendTimeout(), "{\"PimLmaSendTimeout\":\"1ms\"}");
        Assert.assertEquals(pimcacheMBean.getPimRetry(), "{\"PimRetry\":1}");

        //Test part2 - load module which is defined in Modules node and not in application node.
        Set<ObjectName> sherpaObjectName = new TreeSet<ObjectName>();
        sherpaObjectName.add(new ObjectName("com.xyz.configs:type=SherpaConfig"));
        JsonNode sherpaNode = Deencapsulation.invoke(cl, "getApplicationConfig", mbsc, "Imap", sherpaObjectName);

        // Set the PimCache MBean values
        Deencapsulation.invoke(cl, "loadModuleConfigs", mbsc, "Imap", sherpaNode, sherpaMbeanName, true);
        Assert.assertEquals(sherpaMbean.getHostName(), "{\"HostName\":\"sherpa-bcp5.dht.xyz.com\"}");
        Assert.assertEquals(sherpaMbean.getPort(), "{\"Port\":4080}");
        
        // Config file missing filergate module configs for imap, we we expect null
        try {
            ObjectName dummy = new ObjectName(ConfigManagerJmx.CONFIG_MBEANS_DOMAIN_PREFIX + "DummyName");
            Assert.assertNull(Deencapsulation.invoke(cl, "loadModuleConfigs", mbsc, "Imap", imapNode, dummy, true));
            Assert.fail("Should not get here, DummyMBeanName config module should be missing");
        } catch ( Exception e ) {
            Assert.assertTrue(e instanceof ConfigException);
        }
        vm.close();
    }

    @Test
    public void hasAttribute() throws org.codehaus.jackson.JsonParseException, JsonMappingException, IOException {
        String filename = "./src/test/resources/autoconf.json";
        File f = new File(filename);
        ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
        JsonNode configNode = mapper.readValue(f, JsonNode.class);
        JsonNode filerConfig = configNode.get("app1").get("LocalConfig");
        Boolean checkPassed = Deencapsulation.invoke(ConfigLoaderJmx.class, "hasAttribute", filerConfig,
                "FilerGateServerName");
        Assert.assertTrue(checkPassed);

        checkPassed = Deencapsulation.invoke(ConfigLoaderJmx.class, "hasAttribute", filerConfig, "fakename");
        Assert.assertFalse(checkPassed);

        checkPassed = Deencapsulation.invoke(ConfigLoaderJmx.class, "hasAttribute", filerConfig, "FilerGateTimeout");
        Assert.assertTrue(checkPassed);
        checkPassed = Deencapsulation.invoke(ConfigLoaderJmx.class, "hasAttribute", filerConfig, "FilerGateAppId");
        Assert.assertTrue(checkPassed);
    }

    @Test(groups = { "loader" })
    public void setsTest() throws Exception {
        ConfigManagerJvm vm = new ConfigManagerJvm(
                new ObjectName("com.xyz.configmgr:type=ConfigManager,appName=Imap"));

        // get an MBean Server Connection
        vm.attach();
        MBeanServerConnection mbsc = vm.getJMXConnector().getMBeanServerConnection();

        Set<ObjectName> configNames = new TreeSet<ObjectName>();
        configNames.add(new ObjectName("com.xyz.configs:type=FilerGateConfig"));        
        JsonNode filerGateNode = Deencapsulation.invoke(cl, "getApplicationConfig", mbsc, "qmda", configNames);

        // Set the SherpaConfig MBean values
        Deencapsulation.invoke(cl, "loadModuleConfigs", mbsc, "qmda", filerGateNode, filergateMbeanName, true);
        Assert.assertEquals(filergateMbean.getFilerGateTimeout(), "{\"FilerGateTimeout\":\"500ms\",\"_Sets_Type_\":\"FARM\",\"_Sets_\":[]}");
        Assert.assertEquals(filergateMbean.getFilerGateServerPort(), "{\"FilerGateServerPort\":14066,\"_Sets_Type_\":\"FARM\",\"_Sets_\":[]}");
        Assert.assertEquals(filergateMbean.getFilerGateServerName(), "{\"_Sets_Type_\":\"FARM\",\"_Sets_\":[{\"key\":[\"323\"],\"keyList\":{\"FilerGateServerName\":\"fg323.mail.vip.mud.xyz.com\"}},{\"key\":[\"318\"],\"keyList\":{\"FilerGateServerName\":\"fg318.mail.vip.mud.xyz.com\"}},{\"key\":[\"9323\"],\"keyList\":{\"FilerGateServerName\":\"fg9323.mail.vip.ne1.xyz.com\"}}]}");
        Assert.assertEquals(filergateMbean.getFilerGateRetries(), "{\"FilerGateRetries\":1,\"_Sets_Type_\":\"FARM\",\"_Sets_\":[]}");
        Assert.assertEquals(filergateMbean.getFilerGateDirPath(), "{\"FilerGateDirPath\":\"/fg/\",\"_Sets_Type_\":\"FARM\",\"_Sets_\":[]}");
        Assert.assertEquals(filergateMbean.getFilerGateAppId(), "{\"_Sets_Type_\":\"FARM\",\"_Sets_\":[{\"key\":[\"323\"],\"keyList\":{\"FilerGateAppId\":\"xyz.mail.acl.yca.fg-beta\"}},{\"key\":[\"318\"],\"keyList\":{\"FilerGateAppId\":\"xyz.mail.acl.yca.fg-beta\"}},{\"key\":[\"9323\"],\"keyList\":{\"FilerGateAppId\":\"xyz.mail.acl.yca.fg-beta\"}}]}");

    }

    @Test(expectedExceptions = VirtualMachineException.class)
    public void loadMissingAppConfigTest() throws Exception {
        cl.loadAppConfigs(new ObjectName("com.xyz.configmgr:type=ConfigManager,appName=Imap2"), true);
    }

    @Test(groups = { "loader" })
    public void loadAppConfigsTest() throws Exception {

        cl.loadAppConfigs(new ObjectName("com.xyz.configmgr:type=ConfigManager,appName=Imap"), true);

        // Config file has entry for Sherpa module
        Assert.assertEquals(sherpaMbean.getTableName(), "{\"TableName\":\"xyz.contact\"}");

        // Config file missing filergate module configs, we we expect default values
        Assert.assertEquals(pimcacheMBean.getPimRetry(), "{\"PimRetry\":1}");

        // Config file missing filergate module configs, we we expect default values
        Assert.assertEquals(filergateMbean.getFilerGateDirPath(), "error");
    }

    @Test(groups = { "loader" })
    public void loadAppConfigMissingConfigsTest() throws Exception {
        mbs.unregisterMBean(sherpaMbeanName);
        mbs.unregisterMBean(filergateMbeanName);
        mbs.unregisterMBean(pimcacheMbeanName);

        try {
            cl.loadAppConfigs(new ObjectName("com.xyz.configmgr:type=ConfigManager,appName=Imap"), true);
        } catch (ConfigException e) {

            //No Config MBeans registered... should not throw an exception
            Assert.assertTrue(false, "LaodAppConfig threw exception because no config MBeans was registered; this is wrong behavior.");
        }

        //bring them back
        mbs.registerMBean(sherpaMbean, sherpaMbeanName);
        mbs.registerMBean(filergateMbean, filergateMbeanName);
        mbs.registerMBean(pimcacheMBean, pimcacheMbeanName);
    }

    @Test(groups = { "loader" })
    public void loadAppConfigMissingConfigTest() throws Exception {
        cl.loadAppConfigs(new ObjectName("com.xyz.configmgr:type=ConfigManager,appName=Imap"), true);

        // Now verify that the attributes were correctly populated, in this case we got our setting
        // from the pimcache listed under the shared modules section
        Assert.assertEquals(pimcacheMBean.getPimEmdRecvTimeout(), "{\"PimEmdRecvTimeout\":\"4ms\"}");

    }

    // @Test //Enable this test case back when I understand what ammen wanted to
    // test - lafa
    public void lsgConfigTest() throws Exception {

        cl.loadAppConfigs(new ObjectName("com.xyz.configmgr:type=ConfigManager,appName=Imap"), true);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readValue(lsgMbean.getlightsaberServer(), JsonNode.class);

        // lightsaberServer should return json array with "lightsaberServer" elements only
        Assert.assertTrue(node.isArray());
        Assert.assertEquals(node.size(), 4);
        Assert.assertEquals(node.get(0).path(ConfigAdapterJson.CONST.SETS.toString()).getTextValue(), "NONE");

        node = mapper.readValue(lsgMbean.getlightsaberYCA(), JsonNode.class);

        // lightsaberYCA should return json array with "lightsaberYCA" elements only
        Assert.assertTrue(node.isArray());
        Assert.assertEquals(node.size(), 4);
        Assert.assertEquals(node.get(1).path(ConfigAdapterJson.CONST.SETS.toString()).getTextValue(), "xyz.mail.acl.yca.lsg-prod");
    }
}
