package org.commons.jconfig.configloader;

import org.commons.jconfig.config.ConfigException;


/**
 * MBean Registered by @ConfigLoaderApp application. Exposes API for use by applications to trigger configuration
 * loading and synch.<br><br>
 * 
 * An application is eligible for configuration loading by registering a @ConfigManagerMXBean<Br>
 * and registering Configurations MBeans under the org.commons.jconfig.configs domain<br><br>
 * 
 * <b>usage</b><br><br>
 * 
 * MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();<br><br>
 *
 * // Register ConfigManager MBean<br>
 * mbs.registerMBean(new ConfigManager("Imap"), new ObjectName(ConfigManagerVM.CONFIG_MGR_MBEAN_NAME));<br><br>
 * 
 * // Register config MBeans<br>
 * mbs.registerMBean(new FilerGateConfig(), new ObjectName("org.commons.jconfig.configs:type=FilerGateConfig"));<br><br>
 *
 * //get a connection to the ConfigLoader application
 * ConfigLoaderVM vm = new ConfigLoaderVM();<br>
 * vm.Attach();<br>
 * MBeanServerConnection mbsc = vm.getMBeanServerConnection();<br><br>
 * 
 * ObjectName mbeanName = new ObjectName(ConfigLoaderVM.CONFIG_LOADER_MBEAN_NAME);<br><br>
 * 
 * //define and create listener for notification emitted by ConfigLoader<br>
 * ClientListener listener = new ClientListener();<br>
 * mbsc.addNotificationListener(mbeanName, listener, null, null);<br><br>
 *
 * //Invoke the LoadConfigs operation<br>
 * LoaderMXBean mbeanProxy = JMX.newMBeanProxy(mbsc, mbeanName, LoaderMXBean.class, true);<br>
 * mbeanProxy.subscribeConfigs("Imap")<br>
 *
 */
public interface ConfigLoaderJmxMXBean {

    /**
     * Load (and keep up to date) configuration MXBeans registered by a @ConfigManagerMXBean managed application with appName
     * @param appName
     * @throws ConfigLoaderException
     */
    public void subscribeConfigs(String appName) throws ConfigException;

}
