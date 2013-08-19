package org.commons.jconfig.internal.jmx;

import org.commons.jconfig.internal.jmx.LoadModuleConfigsNotification;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoadModuleConfigsNotificationTest {
    
    @Test
    public void testNotification() {
        LoadModuleConfigsNotification notification = new LoadModuleConfigsNotification(this, 1, 123, "some message", "Imap", "SomeModule", true);
        
        Assert.assertEquals(notification.getAppName(), "Imap");
        Assert.assertEquals(notification.getModuleName(), "SomeModule");
        Assert.assertEquals(notification.getMessage(), "some message");
        Assert.assertEquals(notification.getSequenceNumber(), 1);
        Assert.assertEquals(notification.getSource(), this);
        Assert.assertEquals(notification.getResult(), true);
        Assert.assertEquals(notification.getType(), LoadModuleConfigsNotification.MODULE_CONFIGS_TYPE);
    }

}
