package org.commons.jconfig.internal.jmx;


import org.commons.jconfig.internal.jmx.LoadAppConfigsNotification;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoadAppConfigsNotificationTest {

    @Test
    public void testNotification() {
        LoadAppConfigsNotification notification = new LoadAppConfigsNotification(this, 1, 123, "some message", "Imap", false);
        
        Assert.assertEquals(notification.getAppName(), "Imap");
        Assert.assertEquals(notification.getMessage(), "some message");
        Assert.assertEquals(notification.getSequenceNumber(), 1);
        Assert.assertEquals(notification.getSource(), this);
        Assert.assertEquals(notification.getResult(), false);
        Assert.assertEquals(notification.getType(), LoadAppConfigsNotification.APP_CONFIGS_TYPE);
    }
}
