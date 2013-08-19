package org.commons.jconfig.serializers;

import com.google.gson.JsonElement;

import org.commons.jconfig.serializers.ObjectToJsonConverter;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

/**
 * Test to convert CompositeData to JSON Object
 */
public class CompositeDataSerializerTest {

    /**
     * Creates a simple composite data type and uses the json extractor
     * @throws Exception
     */
    @Test
    public void testSimpleCompositeDataExtractor() throws Exception {
        String[] compositePropertyNames = new String[2];
        compositePropertyNames[0] = "Count";
        compositePropertyNames[1] = "Latency";

        final String[] compositePropertyDescriptions = new String[2];
        compositePropertyDescriptions[0] = "Count of requests received for the command";
        compositePropertyDescriptions[1] = "Average Time Taken to process the command";

        final OpenType[] compositePropertyTypes = new OpenType[2];
        compositePropertyTypes[0] = SimpleType.LONG;
        compositePropertyTypes[1] = SimpleType.FLOAT;

        CompositeType compositeType =
                new CompositeType("Command Stats", "Stats for each command",
                        compositePropertyNames, compositePropertyDescriptions,
                        compositePropertyTypes);

        Object[] compositeValues = new Object[compositePropertyNames.length];
        compositeValues[0] = new Long(10);
        compositeValues[1] = new Float(38.34f);

        CompositeData compositeData =
                new CompositeDataSupport(compositeType, compositePropertyNames, compositeValues);

        ObjectToJsonConverter jsonConverter = new ObjectToJsonConverter();

        JsonElement jsonElement = jsonConverter.extractObject(compositeData);
        Assert.assertNotNull(jsonElement);

        JsonElement count = jsonElement.getAsJsonObject().get("Count");
        Assert.assertNotNull(count);
        Assert.assertEquals(count.getAsLong(), 10);
        JsonElement latency = jsonElement.getAsJsonObject().get("Latency");
        Assert.assertNotNull(latency);
        Assert.assertEquals(latency.getAsFloat(), 38.34f);
    }

    /**
     * Creates a composite object inside a composite object and uses the json extractor
     * @throws Exception
     */
    @Test
    public void testComplexCompositeDataExtractor() throws Exception {
        final String[] compositePropertyNames = new String[2];
        compositePropertyNames[0] = "Count";
        compositePropertyNames[1] = "Latency";

        final String[] compositePropertyDescriptions = new String[2];
        compositePropertyDescriptions[0] = "Count of requests received for the command";
        compositePropertyDescriptions[1] = "Average Time Taken to process the command";

        final OpenType[] compositePropertyTypes = new OpenType[2];
        compositePropertyTypes[0] = SimpleType.LONG;
        compositePropertyTypes[1] = SimpleType.FLOAT;

        final CompositeType compositeType =
                new CompositeType("Command Stats", "Stats for each command",
                        compositePropertyNames, compositePropertyDescriptions,
                        compositePropertyTypes);

        final Object[] loginCompositeValues = new Object[compositePropertyNames.length];
        loginCompositeValues[0] = new Long(10);
        loginCompositeValues[1] = new Float(38.34f);

        final Object[] logoutCompositeValues = new Object[compositePropertyNames.length];
        logoutCompositeValues[0] = new Long(30);
        logoutCompositeValues[1] = new Float(26.09f);

        final Object[] capaCompositeValues = new Object[compositePropertyNames.length];
        capaCompositeValues[0] = new Long(89);
        capaCompositeValues[1] = new Float(103.09f);

        final String[] complexCompositePropertyNames = new String[3];
        complexCompositePropertyNames[0] = "Login";
        complexCompositePropertyNames[1] = "Logout";
        complexCompositePropertyNames[2] = "Capability";

        CompositeData[] complexDataValues = new CompositeData[complexCompositePropertyNames.length];
        complexDataValues[0] =
                new CompositeDataSupport(compositeType, compositePropertyNames, loginCompositeValues);
        complexDataValues[1] =
                new CompositeDataSupport(compositeType, compositePropertyNames, logoutCompositeValues);
        complexDataValues[2] =
                new CompositeDataSupport(compositeType, compositePropertyNames, capaCompositeValues);

        final OpenType[] complexCompositePropertyTypes = new OpenType[3];
        complexCompositePropertyTypes[0] = compositeType;
        complexCompositePropertyTypes[1] = compositeType;
        complexCompositePropertyTypes[2] = compositeType;

        final String[] complexCompositePropertyDescriptions = new String[3];
        complexCompositePropertyDescriptions[0] =
                "Command Stats for " + complexCompositePropertyNames[0];
        complexCompositePropertyDescriptions[1] =
                "Command Stats for " + complexCompositePropertyNames[1];
        complexCompositePropertyDescriptions[2] =
                "Command Stats for " + complexCompositePropertyNames[2];

        final CompositeType complexCompositeType =
                new CompositeType("Commands", "Stats for all commands",
                        complexCompositePropertyNames, complexCompositePropertyDescriptions,
                        complexCompositePropertyTypes);

        CompositeData complexData =
                new CompositeDataSupport(complexCompositeType, complexCompositePropertyNames,
                        complexDataValues);

        ObjectToJsonConverter jsonConverter = new ObjectToJsonConverter();
        JsonElement jsonElement = jsonConverter.extractObject(complexData);
        Assert.assertNotNull(jsonElement);

        JsonElement capability = jsonElement.getAsJsonObject().get("Capability");
        Assert.assertNotNull(capability);
        JsonElement count = capability.getAsJsonObject().get("Count");
        Assert.assertNotNull(count);
        Assert.assertEquals(count.getAsLong(), 89);
        JsonElement latency = capability.getAsJsonObject().get("Latency");
        Assert.assertNotNull(latency);
        Assert.assertEquals(latency.getAsFloat(), 103.09f);

        JsonElement login = jsonElement.getAsJsonObject().get("Login");
        Assert.assertNotNull(login);
        count = login.getAsJsonObject().get("Count");
        Assert.assertNotNull(count);
        Assert.assertEquals(count.getAsLong(), 10);
        latency = login.getAsJsonObject().get("Latency");
        Assert.assertNotNull(latency);
        Assert.assertEquals(latency.getAsFloat(), 38.34f);

        JsonElement logout = jsonElement.getAsJsonObject().get("Logout");
        Assert.assertNotNull(logout);
        count = logout.getAsJsonObject().get("Count");
        Assert.assertNotNull(count);
        Assert.assertEquals(count.getAsLong(), 30);
        latency = logout.getAsJsonObject().get("Latency");
        Assert.assertNotNull(latency);
        Assert.assertEquals(latency.getAsFloat(), 26.09f);
    }

    @Test
    public void testTabularDataExtractor() throws Exception {
        final String[] compositePropertyNames = new String[3];
        compositePropertyNames[0] = "Command";
        compositePropertyNames[1] = "Count";
        compositePropertyNames[2] = "Latency";

        final String[] compositePropertyDescriptions = new String[3];
        compositePropertyDescriptions[0] = "Name of the command";
        compositePropertyDescriptions[1] = "Count of requests received for the command";
        compositePropertyDescriptions[2] = "Average Time Taken to process the command";

        final OpenType[] compositePropertyTypes = new OpenType[3];
        compositePropertyTypes[0] = SimpleType.STRING;
        compositePropertyTypes[1] = SimpleType.LONG;
        compositePropertyTypes[2] = SimpleType.FLOAT;

        final CompositeType compositeType =
                new CompositeType("Command Stats", "Stats for each command",
                        compositePropertyNames, compositePropertyDescriptions,
                        compositePropertyTypes);

        String[] tabularIndexNames = new String[3];
        tabularIndexNames[0] = compositePropertyNames[0];
        tabularIndexNames[1] = compositePropertyNames[0];
        tabularIndexNames[2] = compositePropertyNames[0];
        TabularType commandTabularType =
                new TabularType("Command Stats", "Command Stats for Various IMAP Commands",
                        compositeType, tabularIndexNames);

        TabularDataSupport tabularData =
                new TabularDataSupport(commandTabularType);

        CompositeData[] commandComposites = new CompositeData[3];
        final Object[] loginCompositeValues = new Object[compositePropertyNames.length];
        loginCompositeValues[0] = "LOGIN";
        loginCompositeValues[1] = new Long(10);
        loginCompositeValues[2] = new Float(38.34f);
        commandComposites[0] =
                new CompositeDataSupport(compositeType, compositePropertyNames, loginCompositeValues);

        final Object[] logoutCompositeValues = new Object[compositePropertyNames.length];
        logoutCompositeValues[0] = "LOGOUT";
        logoutCompositeValues[1] = new Long(30);
        logoutCompositeValues[2] = new Float(23.34f);
        commandComposites[1] =
                new CompositeDataSupport(compositeType, compositePropertyNames, logoutCompositeValues);


        final Object[] capabilityCompositeValues = new Object[compositePropertyNames.length];
        capabilityCompositeValues[0] = "CAPABILITY";
        capabilityCompositeValues[1] = new Long(50);
        capabilityCompositeValues[2] = new Float(100.34f);
        commandComposites[2] =
                new CompositeDataSupport(compositeType, compositePropertyNames, capabilityCompositeValues);
        tabularData.putAll(commandComposites);

        ObjectToJsonConverter jsonConverter = new ObjectToJsonConverter();
        JsonElement jsonElement = jsonConverter.extractObject(tabularData);
        Assert.assertNotNull(jsonElement);

        JsonElement capability = jsonElement.getAsJsonObject().get("CAPABILITY");
        Assert.assertNotNull(capability);
        JsonElement command = capability.getAsJsonObject().get("Command");
        Assert.assertNotNull(command);
        Assert.assertEquals(command.getAsString(), "CAPABILITY");
        JsonElement count = capability.getAsJsonObject().get("Count");
        Assert.assertNotNull(count);
        Assert.assertEquals(count.getAsLong(), 50);
        JsonElement latency = capability.getAsJsonObject().get("Latency");
        Assert.assertNotNull(latency);
        Assert.assertEquals(latency.getAsFloat(), 100.34f);

        JsonElement login = jsonElement.getAsJsonObject().get("LOGIN");
        Assert.assertNotNull(login);
        command = login.getAsJsonObject().get("Command");
        Assert.assertNotNull(command);
        Assert.assertEquals(command.getAsString(), "LOGIN");
        count = login.getAsJsonObject().get("Count");
        Assert.assertNotNull(count);
        Assert.assertEquals(count.getAsLong(), 10);
        latency = login.getAsJsonObject().get("Latency");
        Assert.assertNotNull(latency);
        Assert.assertEquals(latency.getAsFloat(), 38.34f);

        JsonElement logout = jsonElement.getAsJsonObject().get("LOGOUT");
        Assert.assertNotNull(logout);
        command = logout.getAsJsonObject().get("Command");
        Assert.assertNotNull(command);
        Assert.assertEquals(command.getAsString(), "LOGOUT");
        count = logout.getAsJsonObject().get("Count");
        Assert.assertNotNull(count);
        Assert.assertEquals(count.getAsLong(), 30);
        latency = logout.getAsJsonObject().get("Latency");
        Assert.assertNotNull(latency);
        Assert.assertEquals(latency.getAsFloat(), 23.34f);
    }

}
