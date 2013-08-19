package org.commons.jconfig.internal.jmx;

import org.commons.jconfig.config.ConfigManager;


/**
 * Implements @ConfigManagerMXBean
 * 
 */
public class ConfigManagerJmx implements ConfigManagerJmxMXBean {

    /**
     * MBean Name identifying Configuration MBeans
     */
    public static final String CONFIG_MBEANS_DOMAIN = "com.yahoo.configs";
    public static final String CONFIG_MBEANS_DOMAIN_PREFIX = "com.yahoo.configs:type=";
    public static final String CONFIG_MBEANS_SEARCH_PATTERN = "com.yahoo.configs:appName=";
    /** store our vmName */
    private final String appName;
    
    /** Current hashCode of config values */
    private int hashCode = 0;
    
    /**
     * 
     * @param appName
     *            Arbitrary string for name of this Application.
     */
    public ConfigManagerJmx(final String appName) {
        this.appName = appName;
    }

    /**
     * @see org.commons.jconfig.internal.jmx.ConfigManagerJmxMXBean#getVMName()
     */
    @Override
    public String getVMName() {
        return appName;
    }

    /* (non-Javadoc)
     * @see com.yahoo.common.config.internal.jmx.ConfigManagerJmxMXBean#getVersion()
     */
    @Override
    public int getConfigHashCode() {
        return hashCode;
    }

    /* (non-Javadoc)
     * @see com.yahoo.common.config.internal.jmx.ConfigManagerJmxMXBean#setVersion(int)
     */
    @Override
    public void updateConfigHashCode(int hashCode) {
        this.hashCode = hashCode;
    }

    /* (non-Javadoc)
     * @see com.yahoo.common.config.internal.jmx.ConfigManagerJmxMXBean#flipCache()
     */
    @Override
    public void flipCache() {
        ConfigManager.INSTANCE.resetAndFlipCache();
    }

}
