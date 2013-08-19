package org.commons.jconfig.configloader;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.commons.jconfig.configloader.ConfigLoaderConfig;
import org.commons.jconfig.datatype.TimeValue;
import org.testng.Assert;
import org.testng.annotations.Test;


public class ConfigLoaderConfigTest {
    @Test
    public void testConfigs() throws IOException {
        ConfigLoaderConfig config = new ConfigLoaderConfig();
        // all defaults
        config.setConfigSyncInterval(new TimeValue(900, TimeUnit.SECONDS));
        config.setConfigFileName("/home/y/conf/ymailAutoConf/ymail.conf");
        config.setJmxFileName("jmx.json");
        config.setConfigServerURL("http://cascade006.mail.ne1.yahoo.com:4080/ymail_configs/");
        config.setJmxReadInterval(new TimeValue(100, TimeUnit.SECONDS));
        config.setMaxWorkerThreads(20);

        Assert.assertEquals(config.getJmxReadInterval().toSeconds(), 100);
        Assert.assertEquals(config.getConfigServerURL(), "http://cascade006.mail.ne1.yahoo.com:4080/ymail_configs/");
        Assert.assertEquals(config.getJmxFileName(), "jmx.json");
        Assert.assertEquals(config.getConfigFileName(), "/home/y/conf/ymailAutoConf/ymail.conf");
        Assert.assertEquals(config.getConfigSyncInterval().toSeconds(), 900);
        Assert.assertEquals(config.getMaxWorkerThreads(), 20);
    }
}
