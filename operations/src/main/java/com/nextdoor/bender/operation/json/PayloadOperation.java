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

package com.nextdoor.bender.operation.json;

import com.google.gson.JsonObject;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.operation.Operation;
import com.nextdoor.bender.operation.OperationException;

public abstract class PayloadOperation implements Operation {
  protected abstract void perform(JsonObject obj);

  /**
   * The {@link DeserializedEvent} payload must be a {@link JsonObject}.
   *
   * @param ievent Event that contains a JSON object deserialized payload.
   * @return
   */
  public InternalEvent perform(InternalEvent ievent) {
    DeserializedEvent devent;
    if ((devent = ievent.getEventObj()) == null) {
      return null;
    }
    Object payload = devent.getPayload();

    if (payload == null) {
      return null;
    }

    if (!(payload instanceof JsonObject)) {
      throw new OperationException("Payload data is not a JsonObject");
    }

    perform((JsonObject) payload);

    return ievent;
  }
}
