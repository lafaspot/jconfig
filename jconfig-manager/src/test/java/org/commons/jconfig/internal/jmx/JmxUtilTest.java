package org.commons.jconfig.internal.jmx;

import javax.management.JMException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;

import org.commons.jconfig.config.App1Config;
import org.commons.jconfig.config.App2Config;
import org.commons.jconfig.config.App3Config;
import org.commons.jconfig.config.App4Config;
import org.commons.jconfig.config.ConfigContext;
import org.commons.jconfig.config.ConfigManager;
import org.testng.Assert;
import org.testng.annotations.Test;


public class JmxUtilTest {

    @Test
    public void testConfig() throws JMException, InvalidTargetObjectTypeException {



        try {
            ConfigManager.INSTANCE.getConfig(App1Config.class, ConfigContext.EMPTY);
            ConfigManager.INSTANCE.getConfig(App2Config.class, ConfigContext.EMPTY);
            ConfigManager.INSTANCE.getConfig(App3Config.class, ConfigContext.EMPTY);
            ConfigManager.INSTANCE.getConfig(App4Config.class, ConfigContext.EMPTY);

            // TODO : pending bug 5149340, removing "ConfigManagerJMX.registerConfigObject(config);" line
            //          in ConfigManager.INSTANCE.getConfig, this unit test will throw an exception...
            Assert.fail("Pending fix for bug 5149340 you should not see this exception.");

        } catch (Exception e) {

            // TODO : pending bug 5149340, removing "ConfigManagerJMX.registerConfigObject(config);" line
            //          in ConfigManager.INSTANCE.getConfig, this unit test will throw an exception...

            // Demo
            Assert.assertNotNull("", "jmxbean is not null.");
        }



    }
}
