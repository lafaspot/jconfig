package org.commons.jconfig.internal.jmx;

/**
 * MBean that exposes attributes of a Java Virtual Machine Application
 * with ConfigManager support
 * 
 * @author aabed
 */
public interface ConfigManagerJmxMXBean {
    
    /**
     * 
     * @return The name of the Virtual Machine registering this MBean
     */
    String getVMName();
    
    /**
     * @return the current version of Config values being consumed by application.
     */
    public int getConfigHashCode();
    
    /**
     * @param hashCode Update the version of Config values
     */
    public void updateConfigHashCode(int hashCode);
    
    
    /**
     * Flips ConfigManager cache
     */
    public void flipCache();
    
}
