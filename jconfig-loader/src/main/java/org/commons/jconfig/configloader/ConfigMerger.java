package org.commons.jconfig.configloader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.commons.jconfig.internal.jmx.VirtualMachineException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * ConfigMerger scans all annotated classes loaded and pulls config resource for
 * all those from configLoaderConfig server. It than merges and save merged file
 * to filesystem.
 * 
 * It maintains HashMap of each module and its hashcode to identify if the
 * configLoaderConfig changed. Scanning for config classes is done only when
 * number of jvm process running are changed.
 * 
 * Config Merger merges files with below 2 formats. e.g Input { "Modules": {
 * "org.commons.jconfig.ymail.filergateclient.FilerGateClientConfig": {
 * "FilerGateTimeout": "500ms", "FilerGateRetries": 1 } } } { "lsgclient": {
 * "323": { "lightsaberYCA": "yca.lsg-prod", "lightsaberServer":
 * "ls323.com:4080" } } }
 * 
 * @author jaikit
 * 
 */
public class ConfigMerger {
    private final static Logger logger = Logger.getLogger(ConfigMerger.class);
    private final ConfigLoaderConfig configLoaderConfig;
    private final HttpClient httpClient;
    private final JsonParser parser = new JsonParser();
    // private int configHashCode = 0;
    private final Map<String, Integer> configHashCode = new HashMap<String, Integer>();

    public ConfigMerger(ConfigLoaderConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("ConfigLoaderConfig cannot be null");
        }
        this.configLoaderConfig = config;
        httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter("http.socket.timeout", new Integer(5000));
    }

    private JsonArray getListOfConfigResources() throws ClientProtocolException, IOException {
        JsonArray listOfConfigResources = new JsonArray();
        HttpEntity entity = null;
        try {
            HttpGet getFiles = new HttpGet(configLoaderConfig.getConfigServerURL() + "config_file_list.json");
            logger.info("Fetching config_file_list.json");
            HttpResponse response = null;
            response = httpClient.execute(getFiles);
            entity = response.getEntity();
            String jsonString = null;
            int statusCode = response.getStatusLine().getStatusCode();
            if ((statusCode == 200) && (entity != null)) {
                InputStream instream = entity.getContent();
                StringWriter writer = new StringWriter();
                IOUtils.copy(instream, writer, "UTF-8");
                jsonString = writer.toString();
                JsonObject localConf = (JsonObject) parser.parse(jsonString);
                if (localConf.has("files")) {
                    listOfConfigResources = localConf.get("files").getAsJsonArray();
                } else {
                    throw new IOException("Invalid config_file_list.json format.");
            }
        } else {
                throw new IOException("configLoaderConfig file list not found " + statusCode);
        }
        } finally {
            EntityUtils.consume(entity);
    }
        return listOfConfigResources;
    }

    public void mergeConfig() throws IOException, VirtualMachineException {
        JsonArray configResourceList = getListOfConfigResources();
        JsonObject mergedConf = new JsonObject();
        JsonObject coater = new JsonObject();
        boolean configChange = false;
        for (JsonElement c : configResourceList) {
            logger.info("Fetching configLoaderConfig file: " + c.toString());
            HttpGet httpget = new HttpGet(configLoaderConfig.getConfigServerURL() + c.getAsString());
            HttpResponse response = null;
            response = httpClient.execute(httpget);
            HttpEntity entity = response.getEntity();
            String jsonString = null;
            try {
                int statusCode = response.getStatusLine().getStatusCode();
                if ((statusCode == 200) && (entity != null)) {
                    InputStream instream = entity.getContent();
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(instream, writer, "UTF-8");
                    jsonString = writer.toString();
                    JsonElement localConf = parser.parse(jsonString);
                    if (localConf.isJsonObject()) {
                        for (Map.Entry<String, JsonElement> applicationConfig : localConf.getAsJsonObject().entrySet()) {

                            /*
                             * configs with Modules
                            {
                                "Modules": {
                                    "org.commons.jconfig.filergateclient.FilerGateClientConfig": {
                                        "FilerGateTimeout": "500ms",
                                        "FilerGateRetries": 1
                                    }
                                }
                            } 
                            */                       
                            if (applicationConfig.getKey().equals("Modules")) {
                                JsonElement modules = applicationConfig.getValue();
                                for (Map.Entry<String, JsonElement> elem : modules.getAsJsonObject().entrySet()) {
                                    if (!configHashCode.containsKey(elem.getKey())
                                            || (configHashCode.get(elem.getKey()) != elem.getValue().toString().hashCode())) {
                                        configHashCode.put(elem.getKey(), elem.getValue().toString().hashCode());
                                        configChange = true;
                                        logger.info("Config value changed for module: " + elem.getKey()
                                                    + " and configLoaderConfig value is: " + jsonString);
                                    }
                                    mergedConf.add(elem.getKey(), elem.getValue());
                                }
                            } else {
                                /*
                                 * configLoaderConfig without Modules section
                                {
                                    "lsgclient": {
                                        "323": {
                                            "lightsaberYCA": "org.commons.jconfig.acl.yca.lsg-prod",
                                            "lightsaberServer": "ls323.mail.vip.mud.com:4080"
                                        }
                                    }
                                }
                                */
                                coater.add(applicationConfig.getKey(), applicationConfig.getValue());
                            }                            
                        }
                    } else {
                        logger.error("Incorrect json format for configLoaderConfig " + configLoaderConfig);
                    }

                } else {
                    logger.error("No response from configLoaderConfig server for configLoaderConfig " + c.getAsString()
                            + " and the status code is: "
                            + response.getStatusLine().getStatusCode());
                }
            } finally {
                EntityUtils.consume(entity);
            }
        }
        
        coater.add("Modules", mergedConf);
        if (configChange) {
            saveConfigToFile(coater);
        }
        return;
    }

    /**
     * @param coater
     * @throws IOException
     */
    private void saveConfigToFile(JsonObject coater) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String data = gson.toJson(coater);

        File currFile = new File(configLoaderConfig.getConfigFileName());
        File tmpFile = File.createTempFile("ymail.conf", null, currFile.getParentFile());
        Writer writer = new FileWriter(tmpFile);
        writer.write(data);
        writer.close();

        if (currFile.exists()) {
            if (currFile.delete()) {
                tmpFile.renameTo(currFile);
            } else {
                logger.error("Error deleting file " + currFile.getName() + ". Cannot rename temp file "
                        + tmpFile.getName());
            }
        } else {
            tmpFile.renameTo(currFile);
        }
    }
}
