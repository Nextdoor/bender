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

package com.nextdoor.bender.operation.json.key;

import com.google.gson.JsonObject;
import com.jayway.jsonpath.JsonPath;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.mutator.UnsupportedMutationException;
import com.nextdoor.bender.operation.Operation;
import com.nextdoor.bender.operation.OperationException;

/**
 * Changes the root node of JSON object.
 */
public class JsonRootNodeOperation implements Operation {
  private String path;

  public JsonRootNodeOperation(String path) {
    this.path = path;
  }

  /**
   * The {@link DeserializedEvent} payload must be a {@link JsonObject}.
   *
   * @param event Event with payload to mutate.
   */
  protected void mutateEvent(DeserializedEvent event) throws OperationException {
    Object payload = event.getPayload();

    if (payload == null) {
      return;
    }

    if (!(payload instanceof JsonObject)) {
      throw new OperationException("Payload data is not a JsonObject");
    }

    JsonObject jsonPayload = (JsonObject) payload;
    event.setPayload(JsonPath.read(jsonPayload, path));
  }

  @Override
  public InternalEvent perform(InternalEvent ievent) {
    /*
     * In place mutates an {@InternalEvent}s deserialized payload.
     */
    mutateEvent(ievent.getEventObj());
    return ievent;
  }
}
