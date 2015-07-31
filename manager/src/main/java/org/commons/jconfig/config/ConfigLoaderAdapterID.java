package org.commons.jconfig.config;

/**
 * TODO: remove ConfigLoaderAdapterID dependecy from configmanager Built-In
 * configloader adapter ids.
 * 
 */
public enum ConfigLoaderAdapterID {
    JSON_FILE("config:file:json"), PROPERTIES_FILE("config:file:properties"), JSON_AUTOCONF("config:autoconf:json"), LSG_AUTOCONF(
    "config:autoconf:lsg");

    private ConfigLoaderAdapterID(final String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    private String uri;
}