/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * Copyright 2017 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.operation.json.array;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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

      LinkedHashMap<String, String> partitions = ievent.getPartitions();

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
              new InternalEvent(elm.toString(), ievent.getCtx(), ievent.getArrivalTime());
          DeserializedEvent newDeserEvent = new GenericJsonEvent(elm.getAsJsonObject());
          newEvent.setEventObj(newDeserEvent);

          /*
           * Deep clone the partitions
           */
          if (partitions != null) {
            LinkedHashMap<String, String> newPartitions =
                new LinkedHashMap<String, String>(partitions.size());

            partitions.entrySet().forEach(kv -> {
              newPartitions.put(new String(kv.getKey()), new String(kv.getValue()));
            });

            newEvent.setPartitions(newPartitions);
          }

          output.add(newEvent);
        } catch (Exception e) {
          throw new OperationException(e);
        }
      }

      return output;
    }
  }
}
