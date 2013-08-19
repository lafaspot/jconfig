package org.commons.jconfig.loader.adapters;

import javax.annotation.Nonnull;

import org.codehaus.jackson.JsonNode;
import org.commons.jconfig.config.ConfigException;
import org.commons.jconfig.config.ConfigLoaderAdapterID;


/**
 * 
 * Implements the standard autoConf configuration source defined at
 * 
 * @author aabed
 *
 */
public class AutoConfAdapter implements Adapter {

    @Nonnull
    private final AutoConf autoconf;

    public AutoConfAdapter(@Nonnull final AutoConf autoconf) {
        this.autoconf = autoconf;
    }

    /**
     * @return the uri of the AutoConf adapter
     */
    @Override
    public String getUri() {
        return ConfigLoaderAdapterID.JSON_AUTOCONF.getUri();
    }

    /**
     * Load the standard AutoConf Json returning a Json node that adheres to the
     * standard autoConf Json syntax
     * 
     * @return autoConf JsonNode
     * @throws ConfigSourceException
     */
    @Override
    public JsonNode getModuleNode(final String appName, final String moduleName) throws ConfigException {
        /*
         * look for module in the app parent node first as modules listed
         * directly by the parent takes precedence over modules listed in the
         * common "Modules" section.
         */
        JsonNode moduleNode = autoconf.getModule(appName, moduleName);
        if (moduleNode == null) {
            moduleNode = autoconf.getModule(AutoConfAdapter.MODULES_NODE, moduleName);
        }
        return moduleNode;
    }

    /** autoConf defined node names */
    private static String MODULES_NODE = "Modules";
}
