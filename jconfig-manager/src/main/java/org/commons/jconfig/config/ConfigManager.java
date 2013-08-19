/*
 * Copyright 2011 Yahoo! Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commons.jconfig.config;

import java.lang.annotation.Annotation;

import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.annotation.Nonnull;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.modelmbean.InvalidTargetObjectTypeException;

import org.apache.log4j.Logger;
import org.commons.jconfig.annotations.Config;
import org.commons.jconfig.annotations.ConfigGet;
import org.commons.jconfig.annotations.ConfigResource;
import org.commons.jconfig.annotations.ConfigResourceId;
import org.commons.jconfig.annotations.ConfigSet;
import org.commons.jconfig.datatype.ByteValue;
import org.commons.jconfig.datatype.TimeValue;
import org.commons.jconfig.datatype.TypeFormatException;
import org.commons.jconfig.datatype.ValueType;
import org.commons.jconfig.internal.ConcurrentLRUCache;
import org.commons.jconfig.internal.ConfigAdapter;
import org.commons.jconfig.internal.ConfigAdapterJson;
import org.commons.jconfig.internal.ConfigAdapterProperties;
import org.commons.jconfig.internal.ConfigManagerCache;
import org.commons.jconfig.internal.ScanClassPath;
import org.commons.jconfig.internal.jmx.ConfigManagerJmx;
import org.commons.jconfig.internal.jmx.ConfigManagerJvm;
import org.commons.jconfig.internal.jmx.JmxUtil;
import org.commons.jconfig.internal.jmx.LoadAppConfigsNotification;

import com.google.gson.JsonParser;

/**
 * ConfigManager class is used to create a instance of the config class loaded
 * with the correct values, multiple calls to the ConfigManager for the same
 * config might return the same instance or a different instance depending if
 * the config values where changed of not.
 * 
 * Example code: <code>
 * App1Config config = ConfigManager.INSTANCE.getConfig(App1Config.class);
 * String server = config.getAttachmentServerHost();
 * </code>
 * 
 * The ConfigManager job is to expose the Config object on the JMX layer of the
 * JVM and to return config objects fully loaded with the correct config values,
 * set by the ConfigLoader or loaded from a config file.
 * 
 * By default ConfigManager would use the method name without "get|set" part to
 * load keys from config files. The @ConfigResourceId should be avoided and was
 * added to provide backwards compatibility with old config files.
 * 
 * NOTE: ConfigManager is still under development
 * FIXME fsg 120411 This code has numerous thread-safety problems.
 */
public enum ConfigManager {

    INSTANCE;

    /** Config cache */
    private ConfigManagerCache configManagerCache;

    ConfigManager() {
        configManagerCache = new ConfigManagerCache(this);
    }
    /**
     * the logger
     */
    private final Logger logger = Logger.getLogger(ConfigManager.class);

    /** Config can be registered to JMX only once. This set verifies if config is already registered */
    private final HashSet<String> registerConfigCache = new HashSet<String>();

    /**
     * cache of fetched config objects. Protect with Atomic Reference. Multiple
     * readers (getConfig) Multiple writers (LoadAppConfigsNotification and
     * getConfig). 80000 threads, 200 config instances per thread = 16000000
     * million.
     */
    private final ConcurrentLRUCache<String, Object> configObjectsCache = new ConcurrentLRUCache<String, Object>(10000);

    private final Object waitLoaderLock = new Object();
    private volatile boolean isLoaderDone = false;
    private volatile boolean configManagerInitialized = false;

    private final Object initLock = new Object();
    private volatile int initCount = 0;

    private Set<Class<?>> annotatedClazzez = null;


    /**
     * Generate a set of annotated classes by scanning all path's in classpath
     */
    private void scanAnnotatedClasses() {
        long start = System.nanoTime();
        ScanClassPath<Config> scanClasses = new ScanClassPath<Config>(Config.class, Arrays.asList("org.commons.jconfig."));
        annotatedClazzez  = scanClasses.scanAnnotatedClasses();

        for (Class<?> configClass : annotatedClazzez) {
            try {
                // TODO: maybe this is exposed to early.
                if (!registerConfigCache.contains(configClass.getName())) {
                    JmxUtil.registerConfigObject(this, configClass, getAppName());
                    registerConfigCache.add(configClass.getName());
                }
                logger.info("Registered config " + configClass.getName() + " to JMX");
            } catch (JMException e) {
                throw new ConfigRuntimeException("ConfigManager JMX fatal exception:", e);
            } catch (InvalidTargetObjectTypeException e) {
                throw new ConfigRuntimeException("ConfigManager JMX fatal exception:", e);
            }
        }
        logger.info("ScanClassPath.scanAnnotatedClasses: " + ((System.nanoTime()- start)/1000000) + " ms");
    }

    /**
     * Load config cache from configurations adapters, and registers
     * ConfigManager JMX beans.
     * 
     * @param appName
     */
    private void initialize() {
        if (configManagerInitialized) {
            return;
        }

        synchronized (initLock) {
            while (initCount != 0) {
                if (configManagerInitialized) {
                    return;
                }
                try {
                    initLock.wait(500);
                } catch (InterruptedException e) {
                    throw new ConfigRuntimeException("ConfigManager JMX fatal exception:", e);
                }
            }
            initCount++;
        }

        /* Register config manager bean if not registered yet. */
        try {
            ObjectName beanName = new ObjectName(ConfigManagerJvm.CONFIG_MGR_MBEAN_NAME + getAppName());
            if (!ManagementFactory.getPlatformMBeanServer().isRegistered(beanName)) {
                try {
                    ManagementFactory.getPlatformMBeanServer().registerMBean(new ConfigManagerJmx(getAppName()),
                            beanName);
                } catch (InstanceAlreadyExistsException e) {
                    throw new ConfigRuntimeException("Failed to register JMX bean: "
                            + ConfigManagerJvm.CONFIG_MGR_MBEAN_NAME + getAppName(),
                            e);
                } catch (MBeanRegistrationException e) {
                    throw new ConfigRuntimeException("Failed to register JMX bean: "
                            + ConfigManagerJvm.CONFIG_MGR_MBEAN_NAME + getAppName(),
                            e);
                } catch (NotCompliantMBeanException e) {
                    throw new ConfigRuntimeException("Failed to register JMX bean: "
                            + ConfigManagerJvm.CONFIG_MGR_MBEAN_NAME + getAppName(),
                            e);
                }
            }
        } catch (MalformedObjectNameException e) {
            throw new ConfigRuntimeException("ConfigManager JMX fatal exception:", e);
        }

        try {
            getInternalConfig();

            logger.info("Resize config cache to " + internalConfig.getMaxCacheSize().intValue() + ".");
            // resize cache to config value
            configObjectsCache.setMaxSize(internalConfig.getMaxCacheSize().intValue());

            if (annotatedClazzez == null) {
                scanAnnotatedClasses();
            }

            if (internalConfig.getLoadFrom().equals("JMX")) {
                isLoaderDone = false;
                long endTime = System.currentTimeMillis() + internalConfig.getConfigLoaderSyncInterval().toMillis();
                synchronized (waitLoaderLock) {
                    try {
                        logger.error("Waiting for ConfigLoader to set the values for "
                                + internalConfig.getConfigLoaderSyncInterval().toSeconds() + "s");
                        while (!isLoaderDone && (endTime > System.currentTimeMillis())) {
                            waitLoaderLock.wait(500);
                        }
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                        throw new ConfigRuntimeException("Failed to attach to ConfigLoader jvm: ", e);
                    }

                }
                if (!isLoaderDone) {
                    throw new ConfigRuntimeException("Failed to load config from ConfigLoader after "
                            + internalConfig.getConfigLoaderSyncInterval());
                }

            } else {
                for (Class<?> configClass : annotatedClazzez) {
                    ConfigResource anno = configClass.getAnnotation(ConfigResource.class);
                    ConfigAdapter<String> configAdapter = null;
                    if (anno != null) {
                        String uri = anno.name();
                        if (uri != null) {
                            try {
                                if (uri.toLowerCase().endsWith(".json")) {
                                    configAdapter = new ConfigAdapterJson(uri, UTF8, internalConfig);
                                    configAdapter.loadValue(configManagerCache);
                                } else if (uri.toLowerCase().endsWith(".properties")) {
                                    configAdapter = new ConfigAdapterProperties(uri, UTF8, internalConfig);
                                    configAdapter.loadValue(configManagerCache);
                                }
                            } catch (ConfigException e) {
                                /* Catch here because we do not want to fail initialize if one config is bad */
                                logger.error("Error loading config " + uri + " ", e);
                            }
                        }
                    }
                }
                //trigger flipping of cache
                resetAndFlipCache();
            }
            configManagerInitialized = true;
        } finally {
            initCount--;
        }
    }

    private String appName = "ConfigManager_" + System.nanoTime();

    private String getAppName() {
        return appName;
    }

    /**
     * LoadAppConfigsNotification succeeded
     * 
     */
    public void resetAndFlipCache() {
        configManagerCache.flipCache();
        configObjectsCache.clear();
        setLoadingDone();
    }

    /**
     * Inner class that will handle the notifications from the configLoader
     */
    public class ConfigLoaderListener implements NotificationListener {

        private final ConfigManager manager;

        ConfigLoaderListener(final ConfigManager manager) {
            this.manager = manager;
        }

        @Override
        public void handleNotification(final Notification notification, final Object handback) {

            if (notification instanceof LoadAppConfigsNotification) {
                LoadAppConfigsNotification acn = (LoadAppConfigsNotification) notification;
                if (acn.getAppName().equals(manager.getAppName())) {
                    if (acn.getResult() == true) {

                        logger.info("ConfigLoader LoadAppConfigsNotification for " + manager.getAppName()
                                + " returned true");

                        ConfigManager.INSTANCE.resetAndFlipCache();

                    } else {
                        logger.error("ConfigLoader LoadAppConfigsNotification for " + manager.getAppName()
                                + " returned false");
                    }
                }
            }
        }
    };

    /**
     * Returns a instance of the Config object fully loaded with the correct
     * values.
     * 
     * Multiple calls to this methods might return the same or a different
     * instance of the config object, depending changes to the config values.
     * 
     * @param <T>
     *            - Config Type
     * @param classDefinition
     *            - local config Class
     * @return - a instance of type <T> of the config class
     * @throws KeyNotFound
     */
    @Deprecated
    protected <T> T getConfig(final Class<T> classDefinition) throws KeyNotFound {
        return getConfig(classDefinition, ConfigContext.EMPTY);
    }

    /**
     * Returns an instance of the specified configuration class {@code ClassDefinition}, fully
     * loaded with all the values specified in the configuration source (file), conditioned by
     * the settings in the specified {@code context}.
     * 
     * @param <T> the type of configuration object returned
     * @param classDefinition the class definition for the desired configuration object
     * @param context an application-specific collection of configuration settings varying
     * according to the application environment (farm, colo, host, etc); if the application
     * has no context adjustments to the configuration, {@code ConfigContext.Empty} may be used.
     * @return an instance of {@code classDefinition} with the appropriate values loaded
     * @throws KeyNotFound if there are inconsistencies in the specification of parameters
     * that prevent loading the returned object with valid values
     */
    @Nonnull
    public <T> T getConfig(final Class<T> classDefinition, final ConfigContext context) throws KeyNotFound {
        // initialize on happens ounce
        initialize();

        SortedSet<String> contextSet = configManagerCache.getContextTypes(classDefinition);

        // return cached entry
        @SuppressWarnings("unchecked")
        T config = (T) configObjectsCache.get(getConfigListEntryName(classDefinition, context, contextSet));
        if (config != null) {
            return config;
        }

        try {
            // Validates if class has @Config annotation
            Config configAnno = getAnnoConfig(classDefinition);
            configAnno.description();
            config = classDefinition.newInstance();

            // Initialize Config Object
            buildConfigObject(config, context, UTF8, configManagerCache);

            configObjectsCache.put(getConfigListEntryName(classDefinition, context, contextSet), config);
            return config;
        } catch (InstantiationException e) {
            throw new ConfigRuntimeException("ConfigManager fatal exception:", e);
        } catch (IllegalAccessException e) {
            throw new ConfigRuntimeException("ConfigManager fatal exception:", e);
        }
    }

    private ConfigManagerConfig internalConfig = null;

    /**
     * Sets the AppName for the current instance of ConfigManager, this is used
     * By the ConfigLoader to select Application specific modules. By default a
     * random AppName will be generated for a ConfigManager Instance. This
     * method also register a JMX MBean with the application name.
     * 
     * @param appName
     */
    public void setAppName(final String appName) {
        this.appName = appName;
        /* Register our MBean in the JVM */
        try {
            ObjectName name = new ObjectName(ConfigManagerJvm.CONFIG_MGR_MBEAN_NAME + getAppName());
            if (ManagementFactory.getPlatformMBeanServer().isRegistered(name)) {
                ManagementFactory.getPlatformMBeanServer().unregisterMBean(name);
            }

            ManagementFactory.getPlatformMBeanServer().registerMBean(new ConfigManagerJmx(appName), name);
        } catch (InstanceAlreadyExistsException e) {
            throw new ConfigRuntimeException("Failed to register JMX bean: " + ConfigManagerJvm.CONFIG_MGR_MBEAN_NAME
                    + getAppName(),
                    e);
        } catch (MBeanRegistrationException e) {
            throw new ConfigRuntimeException("Failed to register JMX bean: " + ConfigManagerJvm.CONFIG_MGR_MBEAN_NAME
                    + getAppName(),
                    e);
        } catch (NotCompliantMBeanException e) {
            throw new ConfigRuntimeException("Failed to register JMX bean: " + ConfigManagerJvm.CONFIG_MGR_MBEAN_NAME
                    + getAppName(),
                    e);
        } catch (MalformedObjectNameException e) {
            throw new ConfigRuntimeException("Failed to register JMX bean: " + ConfigManagerJvm.CONFIG_MGR_MBEAN_NAME,
                    e);
        } catch (InstanceNotFoundException e) {
            throw new ConfigRuntimeException("Failed to register JMX bean: " + ConfigManagerJvm.CONFIG_MGR_MBEAN_NAME,
                    e);
        }
        if (annotatedClazzez == null) {
            scanAnnotatedClasses();
        }
        initialize();
    }

    /**
     * Fetches ConfigManagerConfig from json file. The config file needs to be in
     * classpath or else config with default values will be initialized.
     */
    private synchronized ConfigManagerConfig getInternalConfig() {
        if (internalConfig == null) {
            ConfigManagerConfig config = new ConfigManagerConfig();
            ConfigResource anno = ConfigManagerConfig.class.getAnnotation(ConfigResource.class);
            ConfigAdapter<String> configAdapter = null;
            ConfigManagerCache localCache = new ConfigManagerCache(this);
            configAdapter = new ConfigAdapterJson(anno.name(), UTF8, config);
            try {
                configAdapter.loadValue(localCache);
            } catch (ConfigException e) {
                logger.warn("Fail to load ConfigManager config :" + anno.name(), e);
            }
            localCache.flipCache();

            buildConfigObject(config, ConfigContext.EMPTY, UTF8, localCache);
            internalConfig = config;
        }
        return internalConfig;
    }

    private static Charset UTF8 = Charset.forName("UTF-8");

    /**
     * Populates config instance with given context
     * 
     * @param config
     *            Config object to be populated by loading the values from
     *            either json or properties file.
     * @param uri
     * @param charset
     * @param localCache
     * @throws KeyNotFound
     */
    public <T> T buildConfigObject(final T config, final ConfigContext context, final Charset charset,
            final ConfigManagerCache localCache)
                    throws KeyNotFound {
        /*
         * Iterate through all the methods and set values
         */
        Method[] methods = config.getClass().getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(ConfigGet.class)) {
                ConfigGet configGet = getAnnoConfigGet(method);
                ValueType valueType = configGet.type();
                configGet.defaultValue();
                String methodName = "set" + method.getName().substring(3);

                @SuppressWarnings("rawtypes")
                Class[] par = new Class[1];
                par[0] = valueType.classDefinition();

                ConfigSet configSet = getConfigSet(config, methodName, par);

                /*
                 * Generate fieldList for which we need to perform lookup. Field
                 * can be specified in 3 formats. 1. Member variable name 2.
                 * Complete address for member variable name along with pkg
                 * names 3. Specified separately by annotation @ConfigResourceId
                 * 
                 * fieldList = [Timeout,
                 * com.yahoo.common.config.App2Config.Timeout]
                 * 
                 * Look for each field in configFormat instance for it value.
                 */
                List<String> fileIdList = new ArrayList<String>();
                try {
                    ConfigResourceId configResId = getAnnoConfigResourceId(config, methodName, par);
                    fileIdList.add(configResId.value());
                } catch (KeyNotFound e) {
                    // If missing just use the other 2 below
                }
                fileIdList.add(method.getName().substring(3));
                fileIdList.add(config.getClass().getName() + "." + method.getName().substring(3));

                if(!localCache.isModuleLoaded(config)) {
                    ConfigResource anno = config.getClass().getAnnotation(ConfigResource.class);
                    String uri = config.getClass().getCanonicalName();
                    if (anno != null) {
                        uri = anno.name();
                    }
                    logger.warn("Config file for module "+ uri +" was not found");
                }

                String value = null;
                for (String fileId : fileIdList) {
                    value = localCache.get(config, context, fileId, null);
                    if (value != null) {
                        break;
                    }
                }
                loadfromValue(config, configGet, configSet, methodName, valueType, value);
            }
        }
        return config;
    }

    /**
     * Converts the value to appropriate datatype and set it.
     * 
     * @param config
     *            Config object whose member variable needs to be set
     * @param methodName
     *            SetMethod which needs to be set
     * @param valueType
     *            DataType of value
     * @param value
     *            Actual value which needs to be set
     * 
     * @throws KeyNotFound
     */
    public <T> void loadfromValue(final Object config, final ConfigGet configGet, final ConfigSet configSet,
            final String methodName, final ValueType valueType, final T value) throws KeyNotFound {

        if (configSet.useDefault()) {
            try {
                if (value != null) {
                    setKey(config, methodName, valueType, value);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Called " + config.getClass().getName() + "." + methodName + "(" + value + ")");
                    }
                } else {
                    setKey(config, methodName, valueType, configGet.defaultValue());
                    if (logger.isTraceEnabled()) {
                        logger.trace("Called " + config.getClass().getName() + "." + methodName + "("
                                + configGet.defaultValue() + ") with default");
                    }
                }
            } catch (TypeFormatException e) {
                // Set to default value in case of exception
                logger.error("Invalid value, using default.", e);
                setKey(config, methodName, valueType, configGet.defaultValue());
                if (logger.isTraceEnabled()) {
                    logger.trace("Called " + config.getClass().getName() + "." + methodName + "("
                            + configGet.defaultValue()
                            + ")  with default");
                }
            }
        } else {
            try {
                if (value != null) {
                    setKey(config, methodName, valueType, value);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Called " + config.getClass().getName() + "." + methodName + "(" + value + ")");
                    }
                } else {
                    throw new KeyNotFound("ConfigManager: key " + methodName.substring(3)
                            + " is required and is currently missing");
                }
            } catch (TypeFormatException e) {
                throw new KeyNotFound("ConfigManager: key " + methodName.substring(3)
                        + " is required, but unabled to parse value ", e);
            }
        }
    }

    /**
     * 
     * @param valueType
     * @param value
     * @return
     */
    private Object convertValue(final ValueType valueType, final String value) {
        if (ValueType.Number == valueType) {
            return Double.parseDouble(value);
        } else if (ValueType.Boolean == valueType) {
            return Boolean.parseBoolean(value);
        } else if (ValueType.String == valueType) {
            return value;
        } else if (ValueType.StringList == valueType) {
            return Arrays.asList(value.split(":"));
        } else if (ValueType.TimeList == valueType) {
            String[] valueArr = value.split(":");
            List<TimeValue> listTimeValue = new ArrayList<TimeValue>();
            for (String timeValue : valueArr) {
                listTimeValue.add(TimeValue.parse(timeValue));
            }
            return listTimeValue;
        } else if (ValueType.Time == valueType) {
            return TimeValue.parse(value);
        } else if (ValueType.Bytes == valueType) {
            return ByteValue.parse(value);
        } else if (ValueType.Json == valueType) {
            JsonParser parser = new JsonParser();
            return parser.parse(value);
        } else {
            throw new ConfigRuntimeException("ConfigManager type " + valueType + " is not supported.");
        }
    }

    /**
     * 
     * @param <T>
     * @param config
     * @param methodName
     * @param valueType
     * @param value
     */
    private <T> void setKey(final Object config, final String methodName, final ValueType valueType, final T value) {

        @SuppressWarnings("rawtypes")
        Class[] par = new Class[1];
        par[0] = valueType.classDefinition();
        Method method = getMethodSet(config, methodName, par);
        try {
            Object[] params = new Object[1];
            if ((value instanceof String) && (ValueType.String != valueType)) {
                params[0] = convertValue(valueType, (String) value);
            } else {
                params[0] = value;
            }
            method.invoke(config, params);
        } catch (TypeFormatException e) {
            e.setKeyName(methodName.substring(3));
            throw e;
        } catch (IllegalArgumentException e) {
            throw new ConfigRuntimeException("ConfigManager fatal exception:", e);
        } catch (IllegalAccessException e) {
            throw new ConfigRuntimeException("ConfigManager fatal exception:", e);
        } catch (InvocationTargetException e) {
            throw new ConfigRuntimeException("ConfigManager fatal exception:", e);
        }
    }

    public <T> boolean containsKey(final Class<T> classDefinition, final String keyName) {
        return containsMethodGet(getConfig(classDefinition, ConfigContext.EMPTY), keyName);
    }

    public <T> Boolean getValueAsBoolean(final Class<T> classDefinition, final String keyName) {
        return (Boolean) getValueAsObject(getConfig(classDefinition, ConfigContext.EMPTY), keyName);
    }

    public <T> Number getValueAsNumber(final Class<T> classDefinition, final String keyName) {
        return (Number) getValueAsObject(getConfig(classDefinition, ConfigContext.EMPTY), keyName);
    }

    public <T> String getValueAsString(final Class<T> classDefinition, final String keyName) {
        return (String) getValueAsObject(getConfig(classDefinition, ConfigContext.EMPTY), keyName);
    }

    /**
     * 
     * @param <T>
     * @param config
     * @param keyName
     * @return
     */
    private <T> boolean containsMethodGet(final T config, final String keyName) {
        Method[] methods = config.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals("get" + keyName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * @param <T>
     * @param config
     * @param keyName
     * @return
     */
    private <T> Object getValueAsObject(final T config, final String keyName) {
        try {
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>) config.getClass();
            Class<?>[] par = null;
            Method method = clazz.getMethod("get" + keyName, par);
            return method.invoke(config, new Object[0]);
        } catch (IllegalArgumentException e) {
            throw new ConfigRuntimeException("ConfigManager fatal exception:", e);
        } catch (IllegalAccessException e) {
            throw new ConfigRuntimeException("ConfigManager fatal exception:", e);
        } catch (InvocationTargetException e) {
            throw new ConfigRuntimeException("ConfigManager fatal exception:", e);
        } catch (SecurityException e) {
            throw new ConfigRuntimeException("ConfigManager fatal exception:", e);
        } catch (NoSuchMethodException e) {
            throw new KeyNotFound("ConfigManager: method get" + keyName
                    + " is required and is currently missing for class " + config.getClass());
        }
    }

    /**
     * 
     * @param <T>
     * @param config
     * @param methodName
     * @param parameterTypes
     * @return
     */
    private <T> ConfigSet getConfigSet(final T config, final String methodName, final Class<?>... parameterTypes) {
        try {
            Method method = config.getClass().getMethod(methodName, parameterTypes);
            if (method.isAnnotationPresent(ConfigSet.class)) {
                return method.getAnnotation(ConfigSet.class);
            } else {
                throw new KeyNotFound("ConfigManager: Method annotation ConfigSet is missing for method " + methodName);
            }
        } catch (SecurityException e) {
            throw new ConfigRuntimeException("ConfigManager: method " + methodToString(methodName, parameterTypes)
                    + " not found in class " + config.getClass(), e);
        } catch (NoSuchMethodException e) {
            throw new KeyNotFound("ConfigManager: method " + methodToString(methodName, parameterTypes)
                    + " is required and is currently missing for class " + config.getClass(), e);
        }
    }

    /**
     * 
     * @param <T>
     * @param config
     * @param methodName
     * @param parameterTypes
     * @return
     */
    private <T> Method getMethodSet(final T config, final String methodName, final Class<?>... parameterTypes) {
        try {
            Method method = config.getClass().getMethod(methodName, parameterTypes);
            if (method.isAnnotationPresent(ConfigSet.class)) {
                return method;
            } else {
                throw new KeyNotFound("ConfigManager: Method annotation ConfigSet is missing for method " + methodName);
            }
        } catch (SecurityException e) {
            throw new ConfigRuntimeException("ConfigManager: method " + methodToString(methodName, parameterTypes)
                    + " not found in class " + config.getClass(), e);
        } catch (NoSuchMethodException e) {
            throw new KeyNotFound("ConfigManager: method " + methodToString(methodName, parameterTypes)
                    + " is required and is currently missing for class " + config.getClass(), e);
        }
    }

    /**
     * 
     * @param methodName
     * @param parameterTypes
     * @return
     */
    private String methodToString(final String methodName, final Class<?>... parameterTypes) {
        StringBuilder params = new StringBuilder();
        boolean first = true;
        for (@SuppressWarnings("rawtypes")
        Class clazz : parameterTypes) {
            if (!first) {
                params.append(", ");
                first = false;
            }
            params.append(clazz.getName());
        }

        return methodName + "(" + params.toString() + ")";
    }

    /**
     * 
     * @param method
     * @return
     */
    private ConfigGet getAnnoConfigGet(final Method method) {
        Annotation[] annotations = method.getDeclaredAnnotations();

        for (Annotation annotation : annotations) {
            if (annotation instanceof ConfigGet) {
                ConfigGet configGet = (ConfigGet) annotation;
                return configGet;
            }
        }
        throw new ConfigRuntimeException("ConfigManager: ConfigGet annotation is missing from method " + method);
    }

    /**
     * 
     * @param <T>
     * @param classDefinition
     * @return
     */
    private <T> Config getAnnoConfig(final Class<T> classDefinition) {
        Annotation[] annotations = classDefinition.getDeclaredAnnotations();

        for (Annotation annotation : annotations) {
            if (annotation instanceof Config) {
                Config config = (Config) annotation;
                return config;
            }
        }
        throw new ConfigRuntimeException("ConfigManager: Class is not annotated with @Config class annotation: "
                + classDefinition.getName());
    }

    /**
     * 
     * @param <T>
     * @param classDefinition
     * @return
     */
    @SuppressWarnings("unused")
    private <T> ConfigResource getAnnoConfigResource(final Class<T> classDefinition) {
        Annotation[] annotations = classDefinition.getDeclaredAnnotations();

        for (Annotation annotation : annotations) {
            if (annotation instanceof ConfigResource) {
                ConfigResource config = (ConfigResource) annotation;
                return config;
            }
        }
        throw new ConfigRuntimeException(
                "ConfigManager: Class is not annotated with @ConfigResource class annotation: "
                        + classDefinition.getName());
    }

    /**
     * 
     * @param config
     * @param methodName
     * @param parameterTypes
     * @return
     */
    private ConfigResourceId getAnnoConfigResourceId(final Object config, final String methodName,
            final Class<?>... parameterTypes) {
        Method method = getMethodSet(config, methodName, parameterTypes);
        if (method.isAnnotationPresent(ConfigResourceId.class) && method.getName().equals(methodName)) {
            return method.getAnnotation(ConfigResourceId.class);
        } else {
            throw new KeyNotFound("ConfigManager: method " + methodName + " is not annotated with @ConfigResourceId");
        }
    }

    /**
     * 
     * 
     * @param <T>
     * @param classDefinition
     * @param SetsKey
     * @return
     */
    private <T> String getConfigListEntryName(final Class<T> classDefinition, final ConfigContext context,
            final SortedSet<String> contextSet) {
        return classDefinition.getName() + context.getUniqueId(contextSet);
    }

    public ConfigManagerCache getCache() {
        return configManagerCache;
    }

    /**
     * Set to true when config values are loaded to Config Manager cache
     */
    public void setLoadingDone() {
        isLoaderDone = true;
    }

}
