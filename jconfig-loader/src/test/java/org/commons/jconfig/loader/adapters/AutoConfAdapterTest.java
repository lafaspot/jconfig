package org.commons.jconfig.loader.adapters;

import java.io.File;

import org.codehaus.jackson.JsonNode;
import org.commons.jconfig.config.ConfigException;
import org.commons.jconfig.config.ConfigLoaderAdapterID;
import org.commons.jconfig.configloader.ConfigLoaderConfig;
import org.commons.jconfig.datatype.TimeValue;
import org.commons.jconfig.loader.adapters.AutoConf;
import org.commons.jconfig.loader.adapters.AutoConfAdapter;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


public class AutoConfAdapterTest {

    private AutoConf autoConf = null;

    @BeforeTest
    public void BeforeTest() throws Exception {
        ConfigLoaderConfig config = new ConfigLoaderConfig();
        // AutoConf test file
        String filename = new File(".").getCanonicalPath() + "/src/test/resources/autoconf.json";
        config.setConfigFileName(filename);
        config.setConfigSyncInterval(TimeValue.parse("60m"));
        autoConf = new AutoConf(config);
    }

    @Test
    public void testAutoConfConfigModuleAdapter() throws ConfigException {
        AutoConfAdapter adapter = new AutoConfAdapter(autoConf);

        Assert.assertEquals(adapter.getUri(), ConfigLoaderAdapterID.JSON_AUTOCONF.getUri());

        JsonNode pimNode = adapter.getModuleNode("Imap", "PimCacheConfig");
        Assert.assertEquals(pimNode.get("PimLmaSendTimeout").getTextValue(), "1ms");

        JsonNode sherpaNode = adapter.getModuleNode("Imap", "SherpaConfig");
        Assert.assertEquals(sherpaNode.get("HostName").getTextValue(), "sherpa-bcp5.dht.xyz.com");
    }
}
