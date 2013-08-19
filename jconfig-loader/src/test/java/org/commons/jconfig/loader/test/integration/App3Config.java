package org.commons.jconfig.loader.test.integration;

import org.commons.jconfig.annotations.Config;
import org.commons.jconfig.annotations.ConfigGet;
import org.commons.jconfig.annotations.ConfigResource;
import org.commons.jconfig.annotations.ConfigResourceId;
import org.commons.jconfig.annotations.ConfigSet;
import org.commons.jconfig.annotations.StringNotEmpty;
import org.commons.jconfig.datatype.ValueType;


@Config(description = "XMAS appbase config object.")
@ConfigResource(name = "app3config.json")
public class App3Config {
    private String sonoraServer = "localhost";

    @ConfigGet(
            description = "Use attachment server hostname",
            type = ValueType.String,
            defaultValue = "localhost"
    )
    @ConfigResourceId(value="DummyResourceId")
    public String getSonoraServer() {
        return sonoraServer;
    }

    @ConfigSet
    @StringNotEmpty
    @ConfigResourceId(value="DummyResourceId")
    public void setSonoraServer(final String value) {
        sonoraServer = value;
    }
    
    private String defaultHost = "localhost";

    @ConfigGet(
            description = "Default host",
            type = ValueType.String,
            defaultValue = "defaulthost.yahoo.com"
    )
    public String getDefaultHost() {
        return defaultHost;
    }

    @ConfigSet
    @StringNotEmpty
    public void setDefaultHost(final String value) {
        defaultHost = value;
    }    

}
