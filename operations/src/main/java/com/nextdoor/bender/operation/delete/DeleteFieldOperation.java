package com.nextdoor.bender.operation.delete;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.operation.EventOperation;
import org.apache.commons.lang3.Validate;

public class DeleteFieldOperation implements EventOperation {
    private final String key;

    public DeleteFieldOperation(String key) {
        Validate.notNull(key);
        this.key = key;
    }

    @Override
    public InternalEvent perform(InternalEvent internalEvent) {
        try {
            deleteKeyFromEvent(internalEvent);
        } catch (Exception e) {
            throw new RuntimeException("JSON field deletion failed.", e);
        }

        return internalEvent;
    }

    //if the field doesn't exist in the JSON, this is a noop based on the JSONPath impl
    private void deleteKeyFromEvent(InternalEvent internalEvent) {
        DeserializedEvent deserializedEvent = internalEvent.getEventObj();
        deserializedEvent.deleteField(key);
    }
}
