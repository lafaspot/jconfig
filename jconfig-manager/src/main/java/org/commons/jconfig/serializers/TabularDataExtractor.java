/**
 * 
 */
package org.commons.jconfig.serializers;

import java.util.Collection;
import java.util.List;

import javax.management.AttributeNotFoundException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author jaikit
 *
 */
public class TabularDataExtractor implements Extractor {

    /*
     * (non-Javadoc)
     * 
     * @see
     * common.config.serializers.Extractor#extractObject(com.yahoo
     * common.config.serializers.ObjectToJsonConverter, java.lang.Object)
     */
    @Override
    public JsonElement extractObject(ObjectToJsonConverter pConverter, Object pValue) throws AttributeNotFoundException {
        TabularData td = (TabularData) pValue;

        return checkForMxBeanMap(td.getTabularType()) ? convertMxBeanMapToJson(td, pConverter)
                : convertTabularDataToJson(td, pConverter);
    }

    /**
     * Check whether the given tabular type represents a MXBean map. See the
     * {@link javax.management.MXBean} specification for details how a map is
     * converted to {@link TabularData} by the MXBean framework.
     * 
     * http://docs.oracle.com/javase/7/docs/api/javax/management/MXBean.html
     * 
     * @param pType
     *            type of tabular data to convert
     * @return true if this type represents an MXBean map, false otherwise.
     */
    private boolean checkForMxBeanMap(TabularType pType) {
        CompositeType rowType = pType.getRowType();
        return rowType.containsKey("key") && rowType.containsKey("value") && rowType.keySet().size() == 2;
    }

    @SuppressWarnings("unchecked")
    private JsonElement convertTabularDataToJson(TabularData pTd, ObjectToJsonConverter pConverter)
            throws AttributeNotFoundException {
        TabularType type = pTd.getTabularType();
        List<String> indexNames = type.getIndexNames();
        JsonObject ret = new JsonObject();
        int index = 0;
        for (CompositeData cd: (Collection<CompositeData>) pTd.values()) {
            JsonElement row = pConverter.extractObject(cd);
            String indexName = indexNames.get(index);
            JsonElement indexKey = pConverter.extractObject(cd.get(indexName));
            ret.add(indexKey.getAsString(), row);
            index++;
        }
        return ret;
    }

    private JsonObject getNextMap(JsonObject pJsonObject, JsonElement pKey) {
        JsonObject ret = (JsonObject) pJsonObject.get(pKey.getAsString());
        if (ret == null) {
            ret = new JsonObject();
            pJsonObject.add(pKey.getAsString(), ret);
        }
        return ret;
    }

    private JsonElement convertMxBeanMapToJson(TabularData pTd, ObjectToJsonConverter pConverter)
            throws AttributeNotFoundException {
        JsonObject ret = new JsonObject();
        for (Object rowObject : pTd.values()) {
            CompositeData row = (CompositeData) rowObject;
            Object keyObject = row.get("key");
            if (keyObject != null) {
                JsonElement value = pConverter.extractObject(row.get("value"));
                ret.add(keyObject.toString(), value);
            }
        }
        return ret;
    }
}
