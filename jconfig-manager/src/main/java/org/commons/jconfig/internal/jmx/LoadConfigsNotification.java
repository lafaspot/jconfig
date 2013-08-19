package org.commons.jconfig.internal.jmx;

public class LoadConfigsNotification extends javax.management.Notification { 
    
    /**
     * 
     */
    private static final long serialVersionUID = -6217394154650491264L;
    
    /**
     * Constructs LoadAppConfigsNotification object.
     *
     * @param type the type string for the notification
     * @param source The notification producer, that is, the MBean the attribute belongs to.
     * @param sequenceNumber The notification sequence number within the source object.
     * @param timeStamp The date at which the notification is being sent.
     * @param msg A String containing the message of the notification.
     * @param appName name of the application the configuration loading was completed for.
     */
    public LoadConfigsNotification(String type, Object source, long sequenceNumber, long timeStamp, String msg, String appName, boolean result) {
        
        super(type, source, sequenceNumber, timeStamp, msg);
        this.appName = appName;
        this.result = result;
    }
    
    /**
     * @return the appName for this instance of the notification
     */
    public String getAppName() {
        return appName;
    }
    
    /**
     * @return the appName for this instance of the notification
     */
    public boolean getResult() {
        return result;
    }

    /**
     * the appName for this instance of the notification
     */
    private String appName;
    
    /**
     * the result of the load operation
     */
    private boolean result;

}
