package org.commons.jconfig.config;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

import org.commons.jconfig.config.ConfigContext;
import org.commons.jconfig.config.ConfigManager;
import org.commons.jconfig.config.KeyNotFound;
import org.commons.jconfig.config.ConfigContext.Entry;
import org.testng.Assert;
import org.testng.annotations.Test;


public class App1ConfigTest {

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

    @Test
    @SuppressWarnings("deprecation")
    public void getWsConfiguration() {
        App1Config c = ConfigManager.INSTANCE.getConfig(App1Config.class);
        assertNotNull(c);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void returnsSingletonInstance() {
        App1Config c1 = ConfigManager.INSTANCE.getConfig(App1Config.class);
        App1Config c2 = ConfigManager.INSTANCE.getConfig(App1Config.class);

        assertSame(c1, c2);
    }

    @Test
    public void contextWithSameKeyValues() {
        ConfigContext context1 = new ConfigContext(new Entry("A", "706"));
        assertEquals(context1.getUniqueId(), "A706");
        ConfigContext context2 = new ConfigContext(new Entry("B", "706"));
        App1Config c1 = ConfigManager.INSTANCE.getConfig(App1Config.class, context1);
        App1Config c2 = ConfigManager.INSTANCE.getConfig(App1Config.class, context2);

        Assert.assertSame(c1, c2);
    }

    @Test
    public void returnDifferentConfigInstancesForDifferentContexts() {
        ConfigContext context1 = new ConfigContext(new Entry("SUBSET1", "706"), new Entry("A", "705"));
        assertEquals(context1.getUniqueId(), "A705SUBSET1706");
        ConfigContext context2 = new ConfigContext(new Entry("SUBSET1", "705"), new Entry("A", "705"));
        App1Config c1 = ConfigManager.INSTANCE.getConfig(App1Config.class, context1);
        App1Config c2 = ConfigManager.INSTANCE.getConfig(App1Config.class, context2);
        App1Config c11 = ConfigManager.INSTANCE.getConfig(App1Config.class, context1);
        App1Config c22 = ConfigManager.INSTANCE.getConfig(App1Config.class, context2);

        assertSame(c1, c11);
        assertSame(c2, c22);
        Assert.assertSame(c1, c2);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void returnsValue() {
        App1Config c = ConfigManager.INSTANCE.getConfig(App1Config.class);

        c.setAttachmentServerHost("localhost");

        String host = c.getAttachmentServerHost();

        assertEquals(host, "localhost");
    }

    @Test
    @SuppressWarnings("deprecation")
    public void returnsInt() {
        App1Config c = ConfigManager.INSTANCE.getConfig(App1Config.class);
        assertEquals(c.getMaxNumberOfConnections().intValue(), 10);
    }
}
