package com.nextdoor.bender.operation.delete;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.operation.EventOperation;
import com.nextdoor.bender.operation.OperationException;

public class DeleteFieldOperation implements EventOperation {
    private final String keyField;

    public DeleteFieldOperation(String keyField) {
        this.keyField = keyField;
    }

    @Override
    public InternalEvent perform(InternalEvent internalEvent) {
        try {
            deleteKeyFromEvent(internalEvent);
        } catch (Exception e) {
            throw new OperationException("JSON field deletion failed for this reason: " + e.getMessage());
        }

        return internalEvent;
    }

    /*
     * if the field doesn't exist in the JSON, this is a noop based on the JSONPath impl
     */
    private void deleteKeyFromEvent(InternalEvent internalEvent) {
        DeserializedEvent deserializedEvent = internalEvent.getEventObj();
        deserializedEvent.deleteField(keyField);
    }
}
