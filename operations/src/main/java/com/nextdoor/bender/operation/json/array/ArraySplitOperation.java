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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.FieldNotFoundException;
import com.nextdoor.bender.deserializer.json.GenericJsonEvent;
import com.nextdoor.bender.operation.MultiplexOperation;
import com.nextdoor.bender.operation.OperationException;

public class ArraySplitOperation implements MultiplexOperation {
  private final String path;
  private final List<String> fieldsToKeep;

  public ArraySplitOperation(String path) {
    this(path, Collections.emptyList());
  }

  public ArraySplitOperation(String path,
                             List<String> fieldsToKeep) {
    this.path = path;
    this.fieldsToKeep = fieldsToKeep;
  }

  @Override
  public List<InternalEvent> perform(InternalEvent ievent) throws OperationException {
    {
      if (ievent.getEventObj() == null) {
        throw new OperationException("Deserialized object is null");
      }

      Object payload;
      try {
        payload = ievent.getEventObj().getField(this.path);
      } catch (FieldNotFoundException e) {
        throw new OperationException(e);
      }

      if (!(payload instanceof JsonArray)) {
        throw new OperationException("Payload data is not a JsonArray");
      }

      LinkedHashMap<String, String> partitions = ievent.getPartitions();

      JsonArray arr = (JsonArray) payload;

      ArrayList<InternalEvent> output = new ArrayList<>();
      for (JsonElement elm : arr) {
        try {
          JsonObject newObject = elm.getAsJsonObject();
          for (String field : this.fieldsToKeep) {
            JsonObject jsonObject = (JsonObject) ievent.getEventObj().getPayload();
            newObject.add(field, jsonObject.get(field));
          }

          InternalEvent newEvent = new InternalEvent(newObject.toString(), ievent.getCtx(), ievent.getArrivalTime());
          DeserializedEvent newDeserEvent = new GenericJsonEvent(newObject);
          newEvent.setEventObj(newDeserEvent);
          newEvent.setEventTime(ievent.getEventTime());

          /*
           * Deep clone the partitions
           */
          if (partitions != null) {
            LinkedHashMap<String, String> newPartitions =
                new LinkedHashMap<>(partitions.size());

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
