package org.commons.jconfig.internal.jmx;

import org.commons.jconfig.internal.jmx.ConfigManagerJmx;
import org.commons.jconfig.internal.jmx.ConfigManagerJmxMXBean;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConfigManagerJmxTest {

    @Test
    public void ConfigManagertest() {
        
        String vmName = "foo"; 
        ConfigManagerJmxMXBean a = new ConfigManagerJmx(vmName);
        
        Assert.assertEquals(a.getVMName(), "foo");
    }
}

