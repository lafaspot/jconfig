package org.commons.jconfig.config;

import org.commons.jconfig.annotations.Config;
import org.commons.jconfig.annotations.ConfigGet;
import org.commons.jconfig.annotations.ConfigResource;
import org.commons.jconfig.annotations.ConfigSet;
import org.commons.jconfig.annotations.NumberRange;
import org.commons.jconfig.datatype.ValueType;

import com.google.gson.JsonObject;

/**
 * This example use a json configuration file instead of a regular java
 * properties file and also uses key values of JSON type. This is not yet
 * supported, but it is here to demonstrate the flexibility, and a basic
 * implementation is included in the ConfigManager for now.
 * 
 * Also check /src/test/resources for the app3.json, to really understand the
 * config file difference.
 * 
 * Please avoid using json config files and json type, it might not be supported
 * in the future.
 * 
 */
@Config(description = "WebService rate limit configuration")
@ConfigResource(name = "app3.json")
public class App3Config {

    private JsonObject mRateLimit;

    /**
     * Example that use a JsonObject with some structure, with multiple values.
     * 
     * @return
     */
    @ConfigGet(
            description = "Thresshold per user for all Jedi weservice APIs.",
            type = ValueType.Json,
            defaultValue = "{}")
            public JsonObject getRateLimit() {
        return mRateLimit;
    }

    /**
     * Example that use a JsonObject to be set in this Key.
     * 
     * @return
     */
    @ConfigSet
    public void setRateLimit(final JsonObject value) {
        mRateLimit = value;
    }

    private Number mUserData;

    @ConfigGet(description = "GetUserData threshold limit per user.", type = ValueType.Number, defaultValue = "2002")
    public Number getUserData() {
        return mUserData;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setUserData(final Number value) {
        mUserData = value;
    }
}
