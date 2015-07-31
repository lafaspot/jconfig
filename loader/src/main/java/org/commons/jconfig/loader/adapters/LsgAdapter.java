package org.commons.jconfig.loader.adapters;

import java.util.Iterator;

import javax.annotation.Nonnull;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.commons.jconfig.config.ConfigException;
import org.commons.jconfig.config.ConfigLoaderAdapterID;
import org.commons.jconfig.internal.ConfigAdapterJson;


/**
 * Adapter for light saber gate configuration.
 * 
 */
public class LsgAdapter implements Adapter {

    @Nonnull
    private final AutoConf autoconf;

    public LsgAdapter(@Nonnull final AutoConf autoconf) {
        this.autoconf = autoconf;
    }

    /**
     * @return the uri of the lsg adapter
     */
    @Override
    public String getUri() {
        return ConfigLoaderAdapterID.LSG_AUTOCONF.getUri();
    }

    /**
     * Load lsg config Json and return a Json node that adheres to the standard
     * autoConf Json syntax
     * 
     * @return autoConf JsonNode. null if "lsgclient" section missing in
     *         autoConf.
     * @throws ConfigSourceException
     */
    @Override
    public JsonNode getModuleNode(final String appName, final String moduleName) throws ConfigException {

        // appName and moduleName unused for lsg module
        // lsg configs found in our standard AutoConf file

        // make sure we have an autoConf file with an "lsgclient" section
        if (!autoconf.hasApplication("lsgclient")) {
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode lsgSets = mapper.createArrayNode();

        JsonNode lsgNode = autoconf.getApplication("lsgclient");
        Iterator<String> farms = lsgNode.getFieldNames();

        while ( farms.hasNext() ) {

            String farmName = farms.next();
            JsonNode oldFarmNode = lsgNode.get(farmName);

            ArrayNode newKeyNode = mapper.createArrayNode();
            ObjectNode newKeyListNode = mapper.createObjectNode();
            ObjectNode newFarmNode = mapper.createObjectNode();

            //{ "key": [ "323" ],
            newKeyNode.add(farmName);
            newFarmNode.put(ConfigAdapterJson.CONST.KEY.toString(), newKeyNode);

            //"keyList": { "FilerGateServerName": "fg323.mail.vip.mud.com", "FilerGateAppId": "mail.acl.yca.fg-beta" }
            Iterator<String> fields = oldFarmNode.getFieldNames();
            while ( fields.hasNext() ) {
                String fieldName = fields.next();
                JsonNode fieldValue = oldFarmNode.get(fieldName);

                //{ "FilerGateServerName": "fg323.mail.vip.mud.com", "FilerGateAppId": "mail.acl.yca.fg-beta" }
                newKeyListNode.put(fieldName, fieldValue);
            }
            newFarmNode.put(ConfigAdapterJson.CONST.KEY_LIST.toString(), newKeyListNode);

            lsgSets.add(newFarmNode);
        }

        // {"Sets": [
        ObjectNode lsgRoot = mapper.createObjectNode();
        lsgRoot.put(ConfigAdapterJson.CONST.SETS.toString(), lsgSets);
        lsgRoot.put(ConfigAdapterJson.CONST.SETS_TYPE.toString(), "FARM");

        return lsgRoot;
    }
}
