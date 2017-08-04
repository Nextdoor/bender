package com.nextdoor.bender.operation.json.array;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.json.GenericJsonEvent;
import com.nextdoor.bender.operation.MultiplexOperation;
import com.nextdoor.bender.operation.OperationException;

public class ArraySplitOperation implements MultiplexOperation {

  @Override
  public List<InternalEvent> perform(InternalEvent ievent) throws OperationException {
    {
      if (ievent.getEventObj() == null) {
        throw new OperationException("Deserialized object is null");
      }

      Object payload = ievent.getEventObj().getPayload();

      if (payload == null) {
        throw new OperationException("Deserialized object is null");
      }

      if (!(payload instanceof JsonArray)) {
        throw new OperationException("Payload data is not a JsonArray");
      }

      JsonArray arr = (JsonArray) payload;

      ArrayList<InternalEvent> output = new ArrayList<InternalEvent>();
      for (JsonElement elm : arr) {
        try {
          InternalEvent newEvent =
              new InternalEvent(output.toString(), ievent.getCtx(), ievent.getArrivalTime());
          DeserializedEvent newDeserEvent = new GenericJsonEvent(elm.getAsJsonObject());
          newEvent.setEventObj(newDeserEvent);
          output.add(newEvent);
        } catch (Exception e) {
          throw new OperationException(e);
        }
      }

      return output;
    }
  }
}
