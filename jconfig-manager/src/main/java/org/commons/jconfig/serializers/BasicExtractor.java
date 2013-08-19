/**
 * 
 */
package org.commons.jconfig.serializers;

import javax.management.AttributeNotFoundException;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * @author jaikit
 *
 */
public class BasicExtractor implements Extractor {

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.yahoo.common.config.serializers.Extractor#extractObject(com.yahoo
     * .common.config.serializers.ObjectToJsonConverter, java.lang.Object)
     */
    @Override
    public JsonElement extractObject(ObjectToJsonConverter converter, Object value) throws AttributeNotFoundException {

        if (value.getClass().isAssignableFrom(String.class)) {
            return new JsonPrimitive((String) value);
        } else if (value.getClass().isAssignableFrom(Boolean.class)) {
            return new JsonPrimitive((Boolean) value);
        } else if (value.getClass().isAssignableFrom(Long.class)) {
            return new JsonPrimitive((Long) value);
        } else if (value.getClass().isAssignableFrom(Short.class)) {
            return new JsonPrimitive((Short) value);
        } else if (value.getClass().isAssignableFrom(Integer.class)) {
            return new JsonPrimitive((Integer) value);
        } else if (value.getClass().isAssignableFrom(Float.class)) {
            return new JsonPrimitive((Float) value);
        } else if (value.getClass().isAssignableFrom(Byte.class)) {
            return new JsonPrimitive((Byte) value);
        } 
        throw new RuntimeException("Type " + value.getClass().getName() + "is not basic type");
    }

}
