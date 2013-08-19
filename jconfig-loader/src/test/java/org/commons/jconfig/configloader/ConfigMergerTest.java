package org.commons.jconfig.configloader;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import mockit.Instantiation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.commons.jconfig.configloader.ConfigLoaderConfig;
import org.commons.jconfig.configloader.ConfigMerger;
import org.commons.jconfig.internal.jmx.VirtualMachineException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * @author jaikit
 * 
 */
public class ConfigMergerTest {
    private final JsonParser parser = new JsonParser();
    private static List<String> configFiles = new ArrayList<String>();

    @MockClass(realClass = HttpResponse.class)
    public static class MockHttpResponse {
        @Mock
        public StatusLine getStatusLine() {
            return Mockit.setUpMock(new MockStatusLine());
        }

        @Mock
        public HttpEntity getEntity() {
            return Mockit.setUpMock(new MockHttpEntity());
        }
    }

    @MockClass(realClass = StatusLine.class)
    public static class MockStatusLine {
        public static int statusCode = 200;

        /**
         * @param statusCode
         *            the statusCode to set
         */
        public static void setStatusCode(final int statusCode) {
            MockStatusLine.statusCode = statusCode;
        }

        @Mock
        public int getStatusCode() {
            return statusCode;
        }
    }

    @MockClass(realClass = DefaultHttpClient.class, instantiation = Instantiation.PerMockSetup)
    public static class MockHttpClient {
        @Mock
        public HttpResponse execute(final HttpUriRequest get, HttpContext context) throws IOException {
            HttpResponse resp = Mockit.setUpMock(new MockHttpResponse());
            return resp;
        }
    }

    @MockClass(realClass = HttpEntity.class, instantiation = Instantiation.PerMockSetup)
    public static class MockHttpEntity {
        static int count = -1;

        @Mock
        public InputStream getContent() throws IllegalStateException {
            JsonObject obj = new JsonObject();
            JsonArray array = new JsonArray();

            obj.add("files", array);
            array.add(new JsonPrimitive("conf1.json"));

            if (count == configFiles.size() - 1)
                count = -1;
            count++;

            return new ByteArrayInputStream(obj.toString().getBytes());
        }
    }

    @BeforeClass
    public void setUp() throws FileNotFoundException, IOException {
    }

    @Test
    public void testMerge() throws IOException, VirtualMachineException {
        ConfigLoaderConfig config = new ConfigLoaderConfig();
        config.setConfigServerURL("http://jcfg.mail.yahoo.com:4080/ymail_confman_configs/com/");
        config.setConfigFileName("/tmp/ymail.conf");
        ConfigMerger merger = new ConfigMerger(config);
        merger.mergeConfig();
    }

    @BeforeMethod
    public void beforeTest() {
        // mock AbstractHttpClient and EntityTemplate classes
        Mockit.setUpMocks(new MockHttpClient());
    }

    @AfterMethod
    public void tearDown() {
        Mockit.tearDownMocks();
    }
}
