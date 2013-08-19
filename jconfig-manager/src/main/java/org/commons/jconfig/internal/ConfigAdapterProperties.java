package org.commons.jconfig.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.commons.jconfig.config.ConfigException;
import org.commons.jconfig.config.ConfigManagerConfig;
import org.commons.jconfig.internal.ConfigAdapterJson.CONST;

import com.google.gson.JsonObject;

public class ConfigAdapterProperties implements ConfigAdapter<String> {

    private final Logger logger = Logger.getLogger(ConfigAdapterJson.class);
    private final Properties props;

    public ConfigAdapterProperties(final String uri, final Charset charset, final ConfigManagerConfig internalConfig) {
        props = new Properties();
        InputStream in = getStream(uri, charset, internalConfig);
        try {
            InputStreamReader reader = new InputStreamReader(in);
            props.load(reader);
            reader.close();
        } catch (FileNotFoundException e) {
            logger.error("Error loading configuration for resource '" + uri + "'.", e);
        } catch (IOException e) {
            logger.error("Error loading configuration for resource '" + uri + "'.", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("Error loading configuration for resource '" + uri + "'.", e);
                }
            }
        }
    }


    private InputStream getStream(final String uri, final Charset charset, final ConfigManagerConfig internalConfig) {
        if (internalConfig != null) {
            File dir = new File(internalConfig.getConfigPath());
            if (dir.isDirectory()) {
                File config = new File(dir.getAbsolutePath() + File.separator + uri);
                if (config.isFile() && config.canRead()) {
                    try {
                        return new FileInputStream(config);
                    } catch (FileNotFoundException e) {
                        logger.warn("File not found: '" + config.getAbsolutePath() + "'");
                    }
                } else {
                    logger.warn("Failed to load configuration for resource '" + config.getAbsolutePath() + "'");
                }
            } else {
                if (logger.isTraceEnabled())
                    logger.trace("File not found: '" + dir.getAbsolutePath() + File.separator + uri + "'");
            }
        }

        // trying classpath
        InputStream in = getClass().getResourceAsStream("/" + uri);
        if (in == null) {
            logger.error("Failed to load configuration for resource '" + uri + "' from classpath.");
        }
        return in;
    }

//    @Override
//    public String get(final Object config, final ConfigContext context, final String fileId,
//            final String defaultValue) {
//        return props.getProperty(fileId, defaultValue);
//    }

    @Override
    public String toString() {
        return props.toString();
    }


    /* (non-Javadoc)
     * @see com.yahoo.common.config.internal.ConfigAdapter#loadValue(com.yahoo.common.config.internal.ConfigAdapterJmx, java.lang.String)
     */
    @Override
    public void loadValue(ConfigManagerCache jmxloader) throws ConfigException{
        Iterator<Object> it = props.keySet().iterator();
        JsonObject json = new JsonObject();
        while (it.hasNext()) 
        {
            String key = (String) it.next();
            json.addProperty(key, props.getProperty(key));
        }
        
        jmxloader.insertValue(CONST._PROP_.toString(), json.toString());
    }
}
