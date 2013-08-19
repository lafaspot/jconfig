package org.commons.jconfig.configloader;

import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.commons.jconfig.annotations.ConfigResource;
import org.commons.jconfig.config.ConfigContext;
import org.commons.jconfig.config.ConfigException;
import org.commons.jconfig.config.ConfigManager;
import org.commons.jconfig.config.ConfigManagerConfig;
import org.commons.jconfig.internal.ConfigAdapterJson;
import org.commons.jconfig.internal.ConfigManagerCache;
import org.commons.jconfig.internal.WorkerExecutorService;
import org.commons.jconfig.internal.jmx.ConfigLoaderJvm;
import org.commons.jconfig.internal.jmx.VirtualMachineException;


/**
 * The Configuration Loader application.
 * 
 * Config Loader application runs as service on client host. One Config Loader
 * services all applications on host.<BR>
 * <BR>
 * 
 * Config Loader registers @ConfigLoaderMXBean JMX MBean with subscribeConfigs
 * operations<Br>
 * <br>
 * 
 * Applications call <i>subscribeConfigs<i> method of @ConfigLoaderMXBean <Br>
 * <br>
 * 
 * Config loader application periodically checks for configuration source
 * changes; changes are detected<br>
 * and pushed to the Application config MBeans that previously called
 * <i>subscribeConfigs<i>.<br>
 * <br>
 * 
 * Config Loader employs modular design to operate on different config sources
 * (properties files, mhl, <br>
 * yinst settings, etc) AutoConf is first source supported; Detects refresh of
 * AutoConf file on local disk<br>
 * 
 * @author jaikit
 */
public class ConfigLoaderRunner {

    private final static Logger logger = Logger.getLogger(ConfigLoaderRunner.class);
    /**
     * Appname for ConfigLoader. This name should be used if any beans owned by ConfigLoader needs to be registered to
     * {@link MBeanServer}
     */
    public static final String CONFIG_LOADER_APP_NAME = "org.commons.jconfig.loader";
    /** Excecutor for pushing config values to individual jvm's */
    private WorkerExecutorService executor;
    /** Scheduler for reading jmx values */
    private final ScheduledExecutorService jmxScheduler = Executors.newScheduledThreadPool(1);
    /** Scheduler for reading config files from config server. */
    private final ScheduledExecutorService confServerReader = Executors.newScheduledThreadPool(1);
    /** ConfigLoader config */
    private ConfigLoaderConfig config;

    /**
     * ConfigLoader config resource directory path. This setting is the only
     * value which is hardcoded.
     */
    String loaderConfigDirPath = "/home/y/conf/yjava_ymail_config_loader";

    /**
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws InstanceAlreadyExistsException, MBeanRegistrationException,
    NotCompliantMBeanException, MalformedObjectNameException, NullPointerException, ConfigException {
        boolean configLoaderNotFound = false;
        try {
            logger.info("Start running Config loader. ");
            ConfigLoaderJvm vm = new ConfigLoaderJvm();
            vm.attach();

            // We are already running, only one instance at a time...
            // TODO : vm.isRunning() call versus Attach attempt and catch
            // exception
            logger.error("Instance of Config Loader already running; this instance will not launch");

            vm.close();

        } catch (VirtualMachineException e) {
            configLoaderNotFound = true;
        }

        // If config loader is running exit this JVM, otherwise start it.
        if (configLoaderNotFound) {
            ConfigLoaderRunner runner = new ConfigLoaderRunner();
            runner.start();
        }
    }

    public void start() throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException,
    MalformedObjectNameException, NullPointerException, ConfigException {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        /* Create ConfigLoaderJmx instance with default constructor, since ConfigLoaderJmx init requires ConfigLoaderConfig which will again result to chicken and egg problem */
        ConfigLoaderJmx mbean = new ConfigLoaderJmx();
        mbs.registerMBean(mbean, new ObjectName(ConfigLoaderJvm.CONFIG_LOADER_MBEAN_NAME));

        loadLoaderConfig();

        mbean.init(config);

        startConfServerReader();

        startJMXReader();
        
        executor = new WorkerExecutorService("ConfigLoaderExecutor", config.getMaxWorkerThreads().intValue());
        try {
            logger.info("Start worker job for pushing configs to applications. ");
            executor.submit(new ConfigLoaderWorker(executor, mbean)).get();
        } catch (InterruptedException e) {
            logger.error("Error running ConfigLoaderWorker ", e);
        } catch (ExecutionException e) {
            logger.error("Error running ConfigLoaderWorker ", e);
        }
    }

    /**
     * Start task to read config files from config server, merge the files and
     * save the merged file
     */
    private void startConfServerReader() {
        logger.info("start reading config files from server " + config.getConfigServerURL());

        final ConfigMerger merger = new ConfigMerger(config);
        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                try {
                    merger.mergeConfig();
                } catch (Exception e) {
                    // any exception thrown will suppress future calls to this
                    // scheduler and hence catch all exceptions.
                    logger.error("error in config server scheduler: ", e);
                }
            }
        };
        logger.info("Start task for reading config files from config server every "
                + config.getConfigServerReadInterval().toSeconds() + "s");
        confServerReader.scheduleAtFixedRate(r1, 0, config.getConfigServerReadInterval().toSeconds(), TimeUnit.SECONDS);

    }

    private static Charset UTF8 = Charset.forName("UTF-8");

    /**
     * @throws ConfigException
     * 
     */
    private void loadLoaderConfig() throws ConfigException {
        logger.info("loading ConfigLoaderConfig ");
        
		config = new ConfigLoaderConfig();
        ConfigManagerConfig configManagerConfig = new ConfigManagerConfig();
        configManagerConfig.setConfigPath(loaderConfigDirPath);
        ConfigResource anno = ConfigLoaderConfig.class.getAnnotation(ConfigResource.class);
        ConfigAdapterJson adapter = new ConfigAdapterJson(anno.name(), Charset.forName("UTF-8"), configManagerConfig);
        ConfigManagerCache localCache = new ConfigManagerCache(ConfigManager.INSTANCE);
        adapter.loadValue(localCache);
        localCache.flipCache();
        ConfigContext context = new ConfigContext();
        context.put("TLD", "com");
        
        ConfigManager.INSTANCE.buildConfigObject(config, context, UTF8,
                localCache);
    }

    /**
     * Start task to read application MBeans
     */
    private void startJMXReader() {
        /*
         * Run JMX reader application in a separate thread. It logs application
         * MBeans to log file. This file is hacked to be read via tomcat
         * application.
         */
        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                try {
                    String[] args = { "-save", config.getJmxFileName() };
                    ConfManCmd.main(args);
                } catch (Exception e) {
                    // any exception thrown will suppress future calls to this
                    // scheduler and hence catch all exceptions.
                    logger.error("error jmx scheduler: ", e);
                }
            }
        };
        logger.info("Start thread for collecting MBeans for applications running after every "
                + config.getJmxReadInterval().toSeconds() + "s");
        jmxScheduler.scheduleAtFixedRate(r1, 30, config.getJmxReadInterval().toSeconds(), TimeUnit.SECONDS);
    }
}
