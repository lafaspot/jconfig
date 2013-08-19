package org.commons.jconfig.configloader;

import org.commons.jconfig.annotations.Config;
import org.commons.jconfig.annotations.ConfigGet;
import org.commons.jconfig.annotations.ConfigLoaderAdapter;
import org.commons.jconfig.annotations.ConfigResource;
import org.commons.jconfig.annotations.ConfigResourceId;
import org.commons.jconfig.annotations.ConfigSet;
import org.commons.jconfig.annotations.StringNotEmpty;
import org.commons.jconfig.config.ConfigLoaderAdapterID;
import org.commons.jconfig.datatype.ValueType;


@Config(description = "LSG config")
@ConfigResource(name = "ymail_xmas_lsg.json")
@ConfigLoaderAdapter(adapter = ConfigLoaderAdapterID.LSG_AUTOCONF)
public class LSGAdapterConfig {

    private String LsgHostName;

    @ConfigGet(
            description = "Best available value for the host name of the LightSaberGate server",
            type = ValueType.String,
            defaultValue = "localhost:4080"
    )
    @ConfigResourceId(value="lightsaberServer")
    public String getLsgHostName() {
        return LsgHostName;
    }

    @ConfigSet
    @StringNotEmpty
    @ConfigResourceId(value="lightsaberServer")
    public void setLsgHostName(final String value) {
        LsgHostName = value;
    }

}
