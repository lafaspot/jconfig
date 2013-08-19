package org.commons.jconfig.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.commons.jconfig.config.ConfigContext;
import org.commons.jconfig.config.ConfigManager;
import org.commons.jconfig.config.ConfigRuntimeException;
import org.commons.jconfig.config.ConfigContext.Entry;
import org.commons.jconfig.internal.ConfigManagerCache;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Test functionality of {@link ConfigManagerCache} by inserting and fetching
 * value
 * 
 * @author jaikit
 * 
 */
@Test(singleThreaded = false)
public class ConfigManagerCacheTest {
    ConfigManagerCache threadSafeAdapter = new ConfigManagerCache(ConfigManager.INSTANCE);
    List<Object> module = new ArrayList<Object>();

    public JsonObject getTestJson(String fileName) throws IOException {
        File config = new File(fileName);
        FileInputStream in = new FileInputStream(config);
        Reader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("utf-8")));
        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(reader);
        reader.close();
        in.close();
        reader = null;
        return json;
    }

    @Test
    public void insertValueValid() throws IOException {
        ConfigManagerCache adapter = new ConfigManagerCache(ConfigManager.INSTANCE);
        JsonObject json = getTestJson("./src/test/resources/configFormatAdapter.json");

        JsonElement test1 = json.get("Test1");
        // use module name as ConfigFormatLoaderAdapter name
        for (JsonElement node : test1.getAsJsonArray()) {
            adapter.insertValue(adapter.getClass().getName(), node.toString());
        }

        adapter.flipCache();
        ConfigContext context1 = new ConfigContext(new Entry("COLO", "bf1"));
        Assert.assertEquals(adapter.get(adapter, context1, "SonoraHostname", ""), "stage1.sonora.bf1.xyz.com");
        Assert.assertEquals(adapter.get(adapter, context1, "SonoraAppId", ""), "invalidappid");

        ConfigContext context2 = new ConfigContext(new Entry("COLO", "ne1"));
        Assert.assertEquals(adapter.get(adapter, context2, "SonoraHostname", ""), "stage1.sonora.ne1.xyz.com");
        Assert.assertEquals(adapter.get(adapter, context2, "SonoraAppId", ""), "yca.stage.app.id.ne1");

        ConfigContext context3 = new ConfigContext(new Entry("COLO", "invalid"));
        Assert.assertEquals(adapter.get(adapter, context3, "SonoraHostname", ""), "localhost");
        Assert.assertEquals(adapter.get(adapter, context3, "SonoraAppId", ""), "invalidappid");

        ConfigContext context4 = new ConfigContext(new Entry("COLO", "ac1"));
        Assert.assertEquals(adapter.get(adapter, context4, "SonoraHostname", ""), "stage1.sonora.ne1.xyz.com");
        Assert.assertEquals(adapter.get(adapter, context4, "SonoraAppId", ""), "yca.stage.app.id.ac1");

    }

    /**
     * Verifies that test throws {@link ConfigRuntimeException} because it tries
     * to insert 2 settype values for same module
     */
    @Test(expectedExceptions = ConfigRuntimeException.class)
    public void insertValueInvalid() throws IOException {
        ConfigManagerCache adapter = new ConfigManagerCache(ConfigManager.INSTANCE);
        JsonObject json = getTestJson("./src/test/resources/configFormatAdapter.json");

        JsonElement test1 = json.get("Test2");
        // use module name as ConfigFormatLoaderAdapter name
        for (JsonElement node : test1.getAsJsonArray()) {
            adapter.insertValue(adapter.getClass().getName(), node.toString());
        }
    }

    @Test
    public void inserValueMultipleModules() throws IOException {
        ConfigManagerCache adapter = new ConfigManagerCache(ConfigManager.INSTANCE);
        JsonObject json = getTestJson("./src/test/resources/configFormatAdapter.json");

        JsonElement test1 = json.get("Test1");
        JsonElement test3 = json.get("Test3");

        // use module name as ConfigFormatLoaderAdapter name
        for (JsonElement node : test1.getAsJsonArray()) {
            adapter.insertValue(adapter.getClass().getName(), node.toString());
        }

        Object obj = new Object();
        // use module name as java Object name
        for (JsonElement node : test3.getAsJsonArray()) {
            adapter.insertValue(obj.getClass().getName(), node.toString());
        }

        adapter.flipCache();
        ConfigContext context1 = new ConfigContext(new Entry("COLO", "bf1"));
        Assert.assertEquals(adapter.get(adapter, context1, "SonoraHostname", ""), "stage1.sonora.bf1.xyz.com");
        Assert.assertEquals(adapter.get(adapter, context1, "SonoraAppId", ""), "invalidappid");
        Assert.assertEquals(adapter.get(obj, context1, "VxHostName", ""), "localhost");
        // Return default value
        Assert.assertEquals(adapter.get(obj, context1, "VxPortNumber", "9999"), "80");
        // Value not set and hence will return default value passed in
        Assert.assertEquals(adapter.get(obj, context1, "VxThreadCount", "99"), "99");

        ConfigContext context2 = new ConfigContext(new Entry("COLO", "ne1"));
        Assert.assertEquals(adapter.get(adapter, context2, "SonoraHostname", ""), "stage1.sonora.ne1.xyz.com");
        Assert.assertEquals(adapter.get(obj, context2, "VxHostName", ""), "vxs.ne1.xyz.com");
        Assert.assertEquals(adapter.get(obj, context2, "VxPortNumber", "9999"), "4080");

    }

    @Test
    public void insertDefaultValues() throws IOException {
        ConfigManagerCache adapter = new ConfigManagerCache(ConfigManager.INSTANCE);
        JsonObject json = getTestJson("./src/test/resources/configFormatAdapter.json");

        JsonElement test4 = json.get("Test4");
        Object obj = new Object();
        // use module name as java Object name
        for (JsonElement node : test4.getAsJsonArray()) {
            adapter.insertValue(obj.getClass().getName(), node.toString());
        }
        adapter.flipCache();
        ConfigContext context2 = new ConfigContext();
        Assert.assertEquals(adapter.get(obj, context2, "VxHostName", ""), "vxs.ne1.xyz.com");
        Assert.assertEquals(adapter.get(obj, context2, "VxPortNumber", "9999"), "80");
    }

    @Test(invocationCount = 1000, threadPoolSize = 1000)
    public void testThreadSafeClass() throws InterruptedException {
        ConfigManagerCache threadSafeAdapter = new ConfigManagerCache(ConfigManager.INSTANCE);
        List<Object> module = new ArrayList<Object>();

        int N = 10;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(N);
        CountDownLatch start1Signal = new CountDownLatch(1);
        CountDownLatch done1Signal = new CountDownLatch(N);

        for (int i = 0; i < N; ++i) {
            // create and start threads
            new Thread(new Worker(startSignal, doneSignal, "vxs.ne1.xyz.com", threadSafeAdapter, module)).start();
        }
        threadSafeAdapter.insertValue(module.getClass().getName(), "{\"VxHostName\":\"vxs.ne1.xyz.com\"}");
        threadSafeAdapter.flipCache();
        startSignal.countDown(); // let all threads proceed

        for (int i = 0; i < N; ++i)
            // create and start threads
            new Thread(new Worker(start1Signal, done1Signal, "vxs.ne2.xyz.com", threadSafeAdapter, module)).start();

        threadSafeAdapter.insertValue(module.getClass().getName(), "{\"VxHostName\":\"vxs.ne2.xyz.com\"}");
        doneSignal.await(); // wait for all to finish
        threadSafeAdapter.flipCache();
        start1Signal.countDown(); // let all threads proceed

        done1Signal.await(); // wait for all to finish
    }

    class Worker implements Runnable {
        private final CountDownLatch startSignal;
        private final CountDownLatch doneSignal;
        private final String hostname;
        private final ConfigManagerCache cache;
        private final List<Object> module;

        Worker(CountDownLatch startSignal, CountDownLatch doneSignal, String hostname, ConfigManagerCache threadSafeAdapter, List<Object> module) {
            this.startSignal = startSignal;
            this.doneSignal = doneSignal;
            this.hostname = hostname;
            this.cache = threadSafeAdapter;
            this.module = module;
        }

        public void run() {
            try {
                startSignal.await();
                ConfigContext context2 = new ConfigContext();
                Assert.assertEquals(this.cache.get(this.module, context2, "VxHostName", ""), this.hostname);
                doneSignal.countDown();
            } catch (InterruptedException ex) {
            } // return;
        }
    }
}
