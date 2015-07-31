package org.commons.jconfig.serializers;

import java.util.HashMap;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularDataSupport;

import org.apache.log4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * @author jaikit
 *
 */
public class ObjectToJsonConverter {
    private final Logger logger = Logger.getLogger(ObjectToJsonConverter.class);
    enum Handler {
        STRING (String.class.getName(), new BasicExtractor()),
        BOOLEAN (Boolean.class.getName(), new BasicExtractor()),
        LONG (Long.class.getName(), new BasicExtractor()),
        SHORT (Short.class.getName(), new BasicExtractor()),
        INTEGER (Integer.class.getName(), new BasicExtractor()),
        FLOAT (Float.class.getName(), new BasicExtractor()),
        BYTE (Byte.class.getName(), new BasicExtractor()),
        TABULARDATASUPPORT (TabularDataSupport.class.getName(), new TabularDataExtractor()),
        COMPOSITEDATASUPPORT (CompositeDataSupport.class.getName(), new CompositeDataExtractor());
        
        String classType;
        Extractor extractor;
        Handler(String classType, Extractor extractor) {
            this.classType = classType;
            this.extractor = extractor;
        }
        private static Map<String, Extractor> extractors = new HashMap<String, Extractor>();
        static {
            for (Handler h : Handler.values()) {
                extractors.put(h.classType, h.extractor);
            }
        }

        static Extractor get(String classType) {
            return extractors.get(classType);
        }
    };
    
    public JsonElement extractObject(Object value) throws AttributeNotFoundException {
        if (null == value) {
            return new JsonPrimitive("null value found for this field.");
        }
        if (Handler.get(value.getClass().getName()) != null) {
            return Handler.get(value.getClass().getName()).extractObject(this, value);
        } else {
            /*
             * Log data type error messages only when debug is enabled. There are many datatypes for which handlers are missing.
             * For e.g
             * [Ljava.lang.String;
             * [Ljavax.management.ObjectName;
             * [Ljavax.management.openmbean.CompositeData;
             */
            if (logger.isDebugEnabled()) {
                logger.error("No Handler found for data type : " + value.getClass().getName());
            }
        }
        return new JsonPrimitive("");
    }
}
