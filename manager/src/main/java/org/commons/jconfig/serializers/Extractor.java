/**
 * 
 */
package org.commons.jconfig.serializers;

import javax.management.AttributeNotFoundException;

import com.google.gson.JsonElement;

/**
 * @author jaikit
 *
 */
public interface Extractor {

    JsonElement extractObject(ObjectToJsonConverter pConverter, Object pValue)
            throws AttributeNotFoundException;

}
