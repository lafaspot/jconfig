package org.commons.jconfig.config;

import javax.management.NotificationListener;

import junit.framework.Assert;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.commons.jconfig.config.ConfigManager;
import org.commons.jconfig.internal.jmx.ConfigLoaderJvm;
import org.commons.jconfig.internal.jmx.VirtualMachineException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.gson.JsonObject;

public class App3ConfigTest {
    @MockClass(realClass = ConfigLoaderJvm.class)
    public static class MockConfigLoaderJvm {
        @Mock
        public void attach() {}

        @Mock
        public void addNotificationListener(final NotificationListener listener) throws VirtualMachineException {}

        @Mock
        public void subscribeConfigs(final String appName) throws VirtualMachineException {
            Deencapsulation.invoke(ConfigManager.INSTANCE, "handleLoadAppConfigsNotification");
        }
    }

    @BeforeClass
    public void setUp() {
        Mockit.setUpMock(new MockConfigLoaderJvm());
    }

    @AfterClass
    public void tearDown() {
        Mockit.tearDownMocks();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void getRatelimit() {
        App3Config config = ConfigManager.INSTANCE.getConfig(App3Config.class);
        JsonObject json = config.getRateLimit();
        Assert.assertNotNull(json.get("Min"));
        Assert.assertNotNull(json.get("Max"));

        int value = json.get("Min").getAsNumber().intValue();
        Assert.assertEquals(value, 6006);

        value = json.get("Max").getAsNumber().intValue();
        Assert.assertEquals(value, 30030);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void getUserData() {
        App3Config config = ConfigManager.INSTANCE.getConfig(App3Config.class);
        Number value = config.getUserData().intValue();
        Assert.assertEquals(value, 2002);
    }
}
