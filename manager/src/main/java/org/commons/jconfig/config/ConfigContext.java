package org.commons.jconfig.config;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Config Context is a key value store used to retrieve key value settings from
 * config files.
 * 
 * -- Ex: creating a config object with a context --
 * 
 * <code>
 * ConfigContext context = new ConfigContext();
 * context.put("FARM", 323);
 * context.put("CLUSTER", "PTO");
 * context.put("USER", "joe");
 * ApplicationConfig config = ConfigManager.INSTANCE.getConfig(AppliactionConfig.class, context);
 * </code>
 * 
 * @author lafa
 */
public class ConfigContext {
    public static class Entry {

        private final String key;
        private final String value;

        public Entry(final String key, final String value) {
            if (key == null) {
                throw new NullPointerException("Config entry key is null.");
            }
            if (value == null) {
                throw new NullPointerException("Config entry key is null.");
            }
            this.key = key;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public String getKey() {
            return key;
        }

    }

    public static final ConfigContext EMPTY = new ConfigContext();

    private final Map<String, Object> dict = new HashMap<String, Object>();

    /**
     * <code>
     * ConfigContext context = new ConfigContext({"FARM","323"},{"CLUSTER","PTO"},{"USER","joe"});
     * ApplicationConfig config = ConfigManager.INSTANCE.getConfig(AppliactionConfig.class, context);
     * <code>
     * 
     * @param values
     */
    public ConfigContext(final Entry... entries) {
        if (entries == null)
            return;

        for (Entry entry : entries) {
            if (entry.getKey() != null && entry.getValue() != null) {
                dict.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Constructor
     */
    public ConfigContext() {
    }

    public void put(final String key, final String value) {
        internalPut(key, value);
    }

    public void put(final String key, final Number value) {
        internalPut(key, value);
    }

    private void internalPut(final String key, final Object value) {
        if (key == null) {
            throw new NullPointerException("key is null.");
        }
        if (value == null) {
            throw new NullPointerException("value of key " + key + " is null.");
        }
        synchronized (dict) {
            dict.put(key, value);
            if (uniqueId != null) {
                uniqueId = null;
            }
        }
    }

    public boolean isEmpty() {
        return dict.isEmpty();
    }

    public String get(final String key) {
        Object value = dict.get(key);
        if (value == null) {
            throw new ConfigRuntimeException("key " + key + " not found in context: " + this);
        } else {
            return value.toString();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#toString()
     */
    @Override
    public String toString() {
        return dict.toString();
    }

    private String uniqueId = null;

    /**
     * Returns uniqueId for a context. Context with the same key-value pair will
     * return the same uniqueId string.
     * 
     * @return
     */
    public String getUniqueId() {
        if (uniqueId != null) {
            return uniqueId;
        } else {
            synchronized (dict) {
                if (uniqueId == null) {
                    StringBuffer temp = new StringBuffer();
                    SortedSet<String> keys = new TreeSet<String>(dict.keySet());
                    for (String key : keys) {
                        temp.append(key);
                        Object value = dict.get(key);
                        temp.append(value == null ? "null" : value.toString());
                    }
                    uniqueId = temp.toString();
                }
            }
        }

        return uniqueId;
    }

    public String getUniqueId(SortedSet<String> contextSet) {
        StringBuffer temp = new StringBuffer();
        SortedSet<String> keys = contextSet;
        for (String key : keys) {
            if (dict.containsKey(key)) {
                temp.append(key);
                Object value = dict.get(key);
                temp.append(value == null ? "null" : value.toString());
            }
        }
        return temp.toString();
    }

}
