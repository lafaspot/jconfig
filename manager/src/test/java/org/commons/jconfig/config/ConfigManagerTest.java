package org.commons.jconfig.config;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import mockit.Deencapsulation;

import org.apache.log4j.Logger;
import org.commons.jconfig.annotations.Config;
import org.commons.jconfig.config.ConfigContext;
import org.commons.jconfig.config.ConfigManager;
import org.commons.jconfig.config.ConfigContext.Entry;
import org.commons.jconfig.internal.ConcurrentLRUCache;
import org.commons.jconfig.internal.ScanClassPath;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


public class ConfigManagerTest {
    private final AtomicInteger count = new AtomicInteger(0);
    private final AtomicLong time = new AtomicLong(0);

    private final ConfigContext contextCacheHit = new ConfigContext(new Entry("SUBSET1", "800"));
    private final Logger logger = Logger.getLogger(this.getClass());

    @BeforeTest
    public void setUp() {
        time.set(0);
        count.set(0);
    }

    @Test
    public void testAnnotateClass() throws ClassNotFoundException, IOException {
        List<Class<?>> clazzez = new ArrayList<Class<?>>();
        Package[] packages = Package.getPackages();
        for (Package lPackage : packages) {
            clazzez.addAll(new ScanClassPath<Config>(Config.class, Arrays.asList("org.commons.jconfig.")).scanPackageAnnotatedClasses(lPackage));
        }
        Assert.assertTrue(clazzez.size() > 0);
    }

    @Test(invocationCount = 1001, threadPoolSize = 100)
    public void getConfigCacheHit() {
        long start = System.nanoTime();
        int i = count.incrementAndGet();
        App2Config config = ConfigManager.INSTANCE.getConfig(App2Config.class, contextCacheHit);
        assertEquals(config.getLocalFarm(), "800");
        long duration = System.nanoTime() - start;
        time.addAndGet(duration);
        logger.info("Invocation CacheHit " + i + " took " + time.get() / 1000000000 + " s.");
    }

    @Test(enabled=false, invocationCount = 1001, threadPoolSize = 100, dependsOnMethods = { "getConfigCacheHit" })
    public void getConfigCacheNotMiss() {
        long start = System.nanoTime();
        int i = count.incrementAndGet();
        ConfigContext context = new ConfigContext(new Entry("SUBSET1", "10"), new Entry("SUBSET2",
                Integer.toString(1000 + i)));
        App2Config config = ConfigManager.INSTANCE.getConfig(App2Config.class, context);
        assertEquals(config.getLocalFarm(), "007");
        long duration = System.nanoTime() - start;
        time.addAndGet(duration);
        logger.info("Invocation CacheMiss " + i + " took " + time.get() / 1000000000 + " s.");
    }

    @Test(enabled=false, dependsOnMethods = { "getConfigCacheNotMiss" })
    public void checkGetConfigCacheNotMiss() {
        ConcurrentLRUCache<String, Object> configObjectsCache = Deencapsulation.getField(ConfigManager.INSTANCE,
                "configObjectsCache");
        assertEquals(configObjectsCache.size(), 2);
    }

    @Test(enabled=false, invocationCount = 1001, threadPoolSize = 100, dependsOnMethods = { "checkGetConfigCacheNotMiss" })
    public void getConfigCacheMiss() {
        long start = System.nanoTime();
        int i = count.incrementAndGet();
        ConfigContext context = new ConfigContext(new Entry("SUBSET1", Integer.toString(1000 + i)), new Entry(
                "SUBSET2", Integer.toString(1000 + i)));
        App2Config config = ConfigManager.INSTANCE.getConfig(App2Config.class, context);
        assertEquals(config.getLocalFarm(), "007");
        long duration = System.nanoTime() - start;
        time.addAndGet(duration);
        logger.info("Invocation CacheMiss " + i + " took " + time.get() / 1000000000 + " s.");
    }

    @Test(enabled=false, dependsOnMethods = { "getConfigCacheMiss" })
    public void checkGetConfigCacheMiss() {
        ConcurrentLRUCache<String, Object> configObjectsCache = Deencapsulation.getField(ConfigManager.INSTANCE,
                "configObjectsCache");
        assertEquals(configObjectsCache.size(), 1003);
    }

}
