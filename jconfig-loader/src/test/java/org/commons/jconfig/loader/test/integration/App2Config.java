package org.commons.jconfig.loader.test.integration;

import org.commons.jconfig.annotations.Config;
import org.commons.jconfig.annotations.ConfigGet;
import org.commons.jconfig.annotations.ConfigResource;
import org.commons.jconfig.annotations.ConfigSet;
import org.commons.jconfig.annotations.StringNotEmpty;
import org.commons.jconfig.datatype.ValueType;


@Config(description = "XMAS appbase config object.")
@ConfigResource(name = "app2config.json")
public class App2Config {

    private Boolean UseAttServer = true;

    @ConfigGet(
            description = "Use attachment Server. Boolean value True/False",
            type = ValueType.Boolean,
            defaultValue = "true"
    )
    public Boolean getUseAttServer() {
        return UseAttServer;
    }

    @ConfigSet
    public void setUseAttServer(final Boolean value) {
        UseAttServer = value;
    }

    private Number MaxNumberOfConnections = 10;

    @ConfigGet(description = "Max number of connections.", type = ValueType.Number, defaultValue = "10")
    public Number getMaxNumberOfConnections() {
        return MaxNumberOfConnections;
    }

    @ConfigSet
    public void setMaxNumberOfConnections(final Number value) {
        MaxNumberOfConnections = value;
    }

    private String AttachmentServerHost = "localhost";

    @ConfigGet(
            description = "Use attachment server hostname",
            type = ValueType.String,
            defaultValue = "localhost"
    )
    public String getAttachmentServerHost() {
        return AttachmentServerHost;
    }

    @ConfigSet
    @StringNotEmpty
    public void setAttachmentServerHost(final String value) {
        AttachmentServerHost = value;
    }

}
