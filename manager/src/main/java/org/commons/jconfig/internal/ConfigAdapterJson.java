package org.commons.jconfig.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.apache.log4j.Logger;
import org.commons.jconfig.config.ConfigException;
import org.commons.jconfig.config.ConfigManagerConfig;
import org.commons.jconfig.config.ConfigRuntimeException;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Encapsulation for parsing and saving Json config. The config values will be
 * saved to instance variable keyMap
 * 
 * Structure of keyMap after parsing: { FARM1 : { a:b, d:e }, FARM2 : {f:d, z:y
 * }, _Defs_ : {a:x} }
 * 
 * @author lafa
 * @author jaikit
 * 
 */
public class ConfigAdapterJson implements ConfigAdapter<String> {
    private final Logger logger = Logger.getLogger(ConfigAdapterJson.class);

    private InputStream getStream(final String uri, final Charset charset,
            final ConfigManagerConfig internalConfig) {
        logger.info("config uri: " + uri);
        File dir = new File(internalConfig.getConfigPath());
        if (dir.isDirectory()) {
            File config = new File(dir.getAbsolutePath() + File.separator + uri);
            if (config.isFile() && config.canRead()) {
                try {
                    return new FileInputStream(config);
                } catch (FileNotFoundException e) {
                    logger.warn("File not found: '" + config.getAbsolutePath()
                            + "'");
                }
            } else {
                logger.warn("Failed to load configuration for resource '"
                        + config.getAbsolutePath() + "'");
            }
        } else {
            if (logger.isTraceEnabled())
                logger.trace("File not found: '" + dir.getAbsolutePath()
                        + File.separator + uri + "'");
        }
        // trying classpath
        InputStream in = getClass().getResourceAsStream("/" + uri);
        if (in == null) {
            in = getClass().getResourceAsStream(uri);
            if (in == null) {
                logger.error("Failed to load configuration for resource '"
                        + uri + "' from classpath.");
            }
        }
        return in;
    }

    private final String uri;
    private final Charset charset;
    private final ConfigManagerConfig internalConfig;

    public ConfigAdapterJson(@Nonnull final String uri,
            @Nonnull final Charset charset,
            @Nonnull final ConfigManagerConfig internalConfig) {
        this.uri = uri;
        this.charset = charset;
        this.internalConfig = internalConfig;
    }

    /**
     * Constant defined in configs of json format
     */
    public static enum CONST {
        DEFAULTS {
            @Override
            public String toString() {
                return "_Defs_";
            }
        },
        SETS {
            @Override
            public String toString() {
                return "_Sets_";
            }
        },
        SETS_TYPE {
            @Override
            public String toString() {
                return "_Sets_Type_";
            }
        },
        KEY_LIST {
            @Override
            public String toString() {
                return "keyList";
            }
        },
        KEY {
            @Override
            public String toString() {
                return "key";
            }
        },
        /**
         * Used in place of default module when adapter is load via properties
         * file.
         * 
         */
        _PROP_ {
            @Override
            public String toString() {
                return "_prop_";
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * common.config.internal.ConfigAdapter#loadValue(common.config.internal
     * .ConfigAdapterJmx, java.lang.String)
     */
    @Override
    public void loadValue(final ConfigManagerCache jmxloader)
            throws ConfigException {
        JsonObject confJson = null;
        InputStream in = getStream(uri, charset, internalConfig);
        if (null == in) {
            throw new ConfigException("Failed to load from json file: " + uri);
        }
        Reader reader = new BufferedReader(new InputStreamReader(in, charset));
        JsonParser parser = new JsonParser();
        try {
            confJson = (JsonObject) parser.parse(reader);
        } catch (JsonIOException e) {
            throw new ConfigException("Invalid json format for file " + uri);
        } catch (JsonSyntaxException e) {
            throw new ConfigException("Invalid json format for file " + uri);
        }
        try {
            reader.close();
            in.close();
        } catch (IOException e) {
            throw new ConfigException("Error while closing json config file ",
                    e);
        }
        if (confJson != null && confJson.get("Modules") != null
                && confJson.get("Modules").isJsonObject()) {
            JsonObject modules = confJson.get("Modules").getAsJsonObject();
            for (Entry<String, JsonElement> entry : modules.entrySet()) {
                if (entry.getValue() != null && entry.getValue().isJsonObject()) {
                    jmxloader.insertValue(entry.getKey(), entry.getValue()
                            .toString());
                } else {
                    throw new ConfigRuntimeException("Invalid config format: "
                            + confJson);
                }
            }
        } else {
            throw new ConfigException(
                    "Config format incorrect or file not found ");
        }
    }
}
