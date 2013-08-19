package org.commons.jconfig.config;

import org.commons.jconfig.annotations.Config;
import org.commons.jconfig.annotations.ConfigGet;
import org.commons.jconfig.annotations.ConfigLoaderAdapter;
import org.commons.jconfig.annotations.ConfigResourceId;
import org.commons.jconfig.annotations.ConfigSet;
import org.commons.jconfig.annotations.StringNotEmpty;
import org.commons.jconfig.datatype.ValueType;


/**
 * This class is a sample implementation of XMAS appbase config.
 * 
 * In this example we expose 3 config values:
 * 
 * UseAttServer - of type "Boolean" with the default value of "true".
 * MaxNumberOfConnections - of type "Number" with default value of "10".
 * AttachmentServerHost - of type "String" with default value of "localhost"
 * 
 * The annotation @ConfigResourceId is used in this example to tell
 * ConfigManager to skip it's default behavior and try to load a key value with
 * the @ConfigResourceId name. By default ConfigManager would use the method
 * name without "get|set" part to load keys from config files. The @ConfigResourceId
 * should be avoided and was added to provide backwards compatibility with old
 * config files.
 * 
 */
@Config(description = "XMAS appbase config object.")
@ConfigLoaderAdapter(uri = "App1Missing.json")
public class App5Config {

    private Boolean mUseAttServer = true;

    @ConfigGet(
            description = "Use attachment Server. Boolean value True/False",
            type = ValueType.Boolean,
            defaultValue = "true"
    )
    @ConfigResourceId("USE_YM_DOWNLOAD_SERVER")
    public Boolean getUseAttServer() {
        return mUseAttServer;
    }

    @ConfigSet
    @ConfigResourceId("USE_YM_DOWNLOAD_SERVER")
    public void setUseAttServer(final Boolean value) {
        mUseAttServer = value;
    }

    private Number mMaxNumberOfConnections = 10;

    @ConfigGet(description = "Max number of connections.", type = ValueType.Number, defaultValue = "10")
    public Number getMaxNumberOfConnections() {
        return mMaxNumberOfConnections;
    }

    @ConfigSet
    public void setMaxNumberOfConnections(final Number value) {
        mMaxNumberOfConnections = value;
    }

    private String mAttachmentServerHost = "localhost";

    @ConfigGet(
            description = "Use attachment server hostname",
            type = ValueType.String,
            defaultValue = "localhost"
    )
    @ConfigResourceId("YM_DOWNLOAD_SERVER")
    public String getAttachmentServerHost() {
        return mAttachmentServerHost;
    }

    @ConfigSet
    @ConfigResourceId("YM_DOWNLOAD_SERVER")
    @StringNotEmpty
    public void setAttachmentServerHost(final String value) {
        mAttachmentServerHost = value;
    }

}
