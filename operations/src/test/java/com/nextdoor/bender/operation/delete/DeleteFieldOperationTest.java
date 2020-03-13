package com.nextdoor.bender.operation.delete;

import com.google.gson.JsonObject;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.json.GenericJsonEvent;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DeleteFieldOperationTest {

    @Test(expected = NullPointerException.class)
    public void testNullField() {
        InternalEvent ievent = new InternalEvent("foo", null, 1);

        DeleteFieldOperation operation = new DeleteFieldOperation(null);
        operation.perform(ievent);
    }

    @Test
    public void testExistingField() {
        InternalEvent ievent = new InternalEvent("foo", null, 1);
        JsonObject actualJsonObject = new JsonObject();
        actualJsonObject.addProperty("ex", "exists");
        GenericJsonEvent jsonEvent = new GenericJsonEvent(actualJsonObject);
        ievent.setEventObj(jsonEvent);

        DeleteFieldOperation operation = new DeleteFieldOperation("ex");
        operation.perform(ievent);

        JsonObject expectedJsonObject = new JsonObject();
        assertEquals(expectedJsonObject, actualJsonObject);
    }

    @Test
    public void testNonExistingField() {
        InternalEvent ievent = new InternalEvent("foo", null, 1);
        JsonObject actualJsonObject = new JsonObject();
        actualJsonObject.addProperty("different", "exists");
        GenericJsonEvent jsonEvent = new GenericJsonEvent(actualJsonObject);
        ievent.setEventObj(jsonEvent);

        DeleteFieldOperation operation = new DeleteFieldOperation("ex");
        operation.perform(ievent);

        JsonObject expectedJsonObject = new JsonObject();
        expectedJsonObject.addProperty("different", "exists");
        assertEquals(expectedJsonObject, actualJsonObject);
    }
}
