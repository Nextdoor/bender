package com.nextdoor.bender.operation.delete;

import com.google.gson.JsonObject;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.json.GenericJsonEvent;
import org.junit.Test;

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
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("ex", "exists");
        GenericJsonEvent jsonEvent = new GenericJsonEvent(jsonObject);
        ievent.setEventObj(jsonEvent);

        DeleteFieldOperation operation = new DeleteFieldOperation("ex");
        operation.perform(ievent);
    }

    @Test
    public void testNonExistingField() {
        InternalEvent ievent = new InternalEvent("foo", null, 1);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("different", "exists");
        GenericJsonEvent jsonEvent = new GenericJsonEvent(jsonObject);
        ievent.setEventObj(jsonEvent);

        DeleteFieldOperation operation = new DeleteFieldOperation("ex");
        operation.perform(ievent);
    }
}
