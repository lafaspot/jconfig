package org.commons.jconfig.configloader;

import static org.testng.Assert.assertEquals;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;

import org.commons.jconfig.config.ConfigContext;
import org.commons.jconfig.config.ConfigContext.Entry;
import org.commons.jconfig.config.ConfigException;
import org.commons.jconfig.config.ConfigManager;
import org.testng.annotations.Test;

public class ConfigLoaderRunnerTest {

    /**
     * Run below integration test by setting log directory and directory where
     * config_loader.json is placed. -DJCONFIG_LOG_DIR=.
     * -DJCONFIG_CDIR=/Users/kinjalkhandhar/git/jconfig/jconfig-loader/conf
     */
    @Test(enabled = false)
    public void testLoaderWithManager() throws InstanceAlreadyExistsException,
            MBeanRegistrationException, NotCompliantMBeanException,
            MalformedObjectNameException, NullPointerException, ConfigException {
        Thread t = new Thread(new RunnerThread());
        t.start();
        ConfigContext context = new ConfigContext(new Entry("SUBSET1", "706"));
        App2Config config = ConfigManager.INSTANCE.getConfig(App2Config.class,
                context);
        assertEquals(config.getLocalCluster(), "myowncluster1");
    }

    private class RunnerThread implements Runnable {

        @Override
        public void run() {
            try {
                ConfigLoaderRunner.main(null);
            } catch (Exception e) {

            }
        }

    }
}
