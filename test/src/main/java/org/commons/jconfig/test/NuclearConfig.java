package org.commons.jconfig.test;

import org.commons.jconfig.annotations.ByteRange;
import org.commons.jconfig.annotations.Config;
import org.commons.jconfig.annotations.ConfigGet;
import org.commons.jconfig.annotations.ConfigResource;
import org.commons.jconfig.annotations.ConfigSet;
import org.commons.jconfig.datatype.ByteValue;
import org.commons.jconfig.datatype.ValueType;

@Config(description = "Nuclear application configuration. ")
@ConfigResource(name = "nuclear.json")
public class NuclearConfig {

    private ByteValue mCacheSize;

    @ConfigGet(description = "Cache size with no default value.", type = ValueType.Bytes, defaultValue = "90Mib")
    public ByteValue getCacheSize() {
        return mCacheSize;
    }

    @ConfigSet()
    @ByteRange(min = "0 MiB", max = "5 MiB")
    public void setCacheSize(final ByteValue value) {
        mCacheSize = value;
    }

    //
    // Config "Sets" values
    //
    private String mHostUrl;

    @ConfigGet(description = "HostUrl \"Sets\" config value", type = ValueType.String, defaultValue = "mail.yahoo.com")
    public String getHostUrl() {
        return mHostUrl;
    }

    @ConfigSet
    public void setHostUrl(final String value) {
        mHostUrl = value;
    }

    private Number mHostPort;

    @ConfigGet(description = "HostPort \"Sets\" config value", type = ValueType.Number, defaultValue = "80")
    public Number getHostPort() {
        return mHostPort;
    }

    @ConfigSet
    public void setHostPort(final Number value) {
        mHostPort = value;
    }

    private String mHostProxy;

    @ConfigGet(description = "HostProxy \"Sets\" config value", type = ValueType.String, defaultValue = "proxy.mail.yahoo.com")
    public String getHostProxy() {
        return mHostProxy;
    }

    @ConfigSet
    public void setHostProxy(final String value) {
        mHostProxy = value;
    }

}
