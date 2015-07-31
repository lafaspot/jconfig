package org.commons.jconfig.config;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import junit.framework.Assert;

import org.commons.jconfig.annotations.ConfigReadKey;
import org.commons.jconfig.config.ConfigContext;
import org.commons.jconfig.config.ConfigManager;
import org.commons.jconfig.config.KeyNotFound;
import org.testng.annotations.Test;


public class App5ConfigTest {

    @Test
    public void testDirectAccessString() {
        App1Config config = ConfigManager.INSTANCE.getConfig(App1Config.class, ConfigContext.EMPTY);

        String server = config.getAttachmentServerHost();
        Assert.assertEquals(server, "localhost");

        config.setAttachmentServerHost("server1");
        Assert.assertEquals(config.getAttachmentServerHost(), "server1");

        // correct the value for other tests
        config.setAttachmentServerHost("localhost");
        Assert.assertEquals(config.getAttachmentServerHost(), "localhost");
    }

    @Test
    public void testDirectAccessBoolean() {
        App1Config config = ConfigManager.INSTANCE.getConfig(App1Config.class, ConfigContext.EMPTY);

        Boolean appId = config.getUseAttServer();
        Assert.assertEquals(appId, new Boolean(true));

        // Example of setting a value directly from config class
        config.setUseAttServer(false);
        Assert.assertEquals(config.getUseAttServer(), new Boolean(false));

        // correct the value for other tests
        config.setUseAttServer(true);
        Assert.assertEquals(config.getUseAttServer(), new Boolean(true));
    }


    @Test
    public void testApp1ConfigIndirectAccessBoolean() {
        // Using ConfigManager to set the Value
        App1Config config = ConfigManager.INSTANCE.getConfig(App1Config.class, ConfigContext.EMPTY);
        config.setUseAttServer(false);

        // Using ConfigManager to get the Value
        if (ConfigManager.INSTANCE.containsKey(App1Config.class, "UseAttServer")) {
            Boolean appId = ConfigManager.INSTANCE.getValueAsBoolean(App1Config.class, "UseAttServer");
            Assert.assertEquals(appId, new Boolean(false));

            // Correct the value for other tests
            config.setUseAttServer(true);
            appId = ConfigManager.INSTANCE.getValueAsBoolean(App1Config.class, "UseAttServer");
            Assert.assertEquals(appId, new Boolean(true));
        } else {
            Assert.fail("missing key");
        }
    }

    @Test
    public void testApp1ConfigIndirectAccessString() {
        // Using ConfigManager to set the Value
        App1Config config = ConfigManager.INSTANCE.getConfig(App1Config.class, ConfigContext.EMPTY);
        config.setAttachmentServerHost("XXX");

        // Using ConfigManager to get the Value
        if (ConfigManager.INSTANCE.containsKey(App1Config.class, "AttachmentServerHost")) {
            String host = ConfigManager.INSTANCE.getValueAsString(App1Config.class, "AttachmentServerHost");
            Assert.assertEquals(host, "XXX");

            // Correct the value for other tests
            config.setAttachmentServerHost("localhost");
            host = ConfigManager.INSTANCE.getValueAsString(App1Config.class, "AttachmentServerHost");
            Assert.assertEquals(host, "localhost");
        } else {
            Assert.fail("missing key");
        }
    }

    @Test
    public void testApp1ConfigIndirectAccessNumber() {
        // Using ConfigManager to set the Value
        App1Config config = ConfigManager.INSTANCE.getConfig(App1Config.class, ConfigContext.EMPTY);
        config.setMaxNumberOfConnections(new Integer(777));

        // Using ConfigManager to get the Value
        if (ConfigManager.INSTANCE.containsKey(App1Config.class, "MaxNumberOfConnections")) {
            Number connections = ConfigManager.INSTANCE.getValueAsNumber(App1Config.class, "MaxNumberOfConnections");
            Assert.assertEquals(connections.intValue(), 777);

            // Correct the value for other tests
            config.setMaxNumberOfConnections(new Integer(10));
            connections = ConfigManager.INSTANCE.getValueAsNumber(App1Config.class, "MaxNumberOfConnections");
            Assert.assertEquals(connections.intValue(), 10);
        } else {
            Assert.fail("missing key");
        }
    }

    @Test
    public void testApp1ConfigIndirectAccessBooleanWithStringAutoConversion() {
        // Using ConfigManager to set the Value
        App1Config config = ConfigManager.INSTANCE.getConfig(App1Config.class, ConfigContext.EMPTY);
        config.setUseAttServer(false);

        // Using ConfigManager to get the Value
        if (ConfigManager.INSTANCE.containsKey(App1Config.class, "UseAttServer")) {
            Boolean appId = ConfigManager.INSTANCE.getValueAsBoolean(App1Config.class, "UseAttServer");
            Assert.assertEquals(appId, new Boolean(false));

            // Correct the value for other tests
            config.setUseAttServer(true);

            appId = ConfigManager.INSTANCE.getValueAsBoolean(App1Config.class, "UseAttServer");
            Assert.assertEquals(appId, new Boolean(true));
        } else {
            Assert.fail("missing key");
        }
    }

    @Test(expectedExceptions = KeyNotFound.class)
    public void testApp1ConfigIndirectAccessGetException() {
        // Using ConfigManager to set the Value
        ConfigManager.INSTANCE.getValueAsBoolean(App1Config.class, "XXXMethod");
    }

    public class ConfigValues {
        public ConfigValues() {
            // TODO Auto-generated constructor stub
        }

        private String mAttachmentServerHost = "xxx";

        public String getmAttachmentServerHost() {
            return mAttachmentServerHost;
        }

        @ConfigReadKey(config=App1Config.class, key="AttachmentServerHost")
        public void setmAttachmentServerHost(final String mAttachmentServerHost) {
            this.mAttachmentServerHost = mAttachmentServerHost;
        }

        private Boolean mUseAttServer = false;

        public Boolean getmUseAttServer() {
            return mUseAttServer;
        }

        @ConfigReadKey(config = App1Config.class, key = "UseAttServer")
        public void setmUseAttServer(final Boolean mUseAttServer) {
            this.mUseAttServer = mUseAttServer;
        }
    }

    @Test
    public void getWsConfiguration() {
        App1Config c = ConfigManager.INSTANCE.getConfig(App1Config.class, ConfigContext.EMPTY);
        assertNotNull(c);
    }

    @Test
    public void returnsSingletonInstance() {
        @SuppressWarnings("deprecation")
        App1Config c1 = ConfigManager.INSTANCE.getConfig(App1Config.class);
        App1Config c2 = ConfigManager.INSTANCE.getConfig(App1Config.class, ConfigContext.EMPTY);

        assertSame(c1, c2);
    }

    @Test
    public void returnsValue() {
        App1Config c = ConfigManager.INSTANCE.getConfig(App1Config.class, ConfigContext.EMPTY);

        c.setAttachmentServerHost("localhost");

        String host = c.getAttachmentServerHost();

        assertEquals(host, "localhost");
    }

    @Test
    public void returnsInt() {
        App1Config c = ConfigManager.INSTANCE.getConfig(App1Config.class, ConfigContext.EMPTY);
        assertEquals(c.getMaxNumberOfConnections().intValue(), 10);
    }
}
