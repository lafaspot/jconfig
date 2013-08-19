package org.commons.jconfig.internal.jmx;


/**
 * Notification that loading configurations for an Application is complete.<BR>
 * Emitted by @ConfigLoaderMXBean. 
 * 
 * @author aabed
 *
 */
public class LoadAppConfigsNotification extends LoadConfigsNotification { 

    /**
     * 
     */
    private static final long serialVersionUID = 2160516591880741330L;
    
    /**
     * 
     */
    public static final String APP_CONFIGS_TYPE = "configLoader.LoadAppConfigsDone";
    public static final String APP_CONFIGS_DESC = "config loading for application is complete";
    
    /**
     * Constructs LoadAppConfigsNotification object.
     *
     * @param source The notification producer, that is, the MBean the attribute belongs to.
     * @param sequenceNumber The notification sequence number within the source object.
     * @param timeStamp The date at which the notification is being sent.
     * @param msg A String containing the message of the notification.
     * @param appName name of the application the configuration loading was completed for.
     */
    public LoadAppConfigsNotification(Object source, long sequenceNumber, long timeStamp, String msg, String appName, boolean result) {
        
        super(LoadAppConfigsNotification.APP_CONFIGS_TYPE, source, sequenceNumber, timeStamp, msg, appName, result);
    }
}
