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
 * Copyright 2016 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.mutator;

import com.google.gson.JsonObject;
import com.nextdoor.bender.deserializer.DeserializedEvent;

public abstract class PayloadMutator implements Mutator {
  protected abstract void mutatePayload(JsonObject obj);

  /**
   * The {@link DeserializedEvent} payload must be a {@link JsonObject}.
   *
   * @param event Event with payload to mutate.
   */
  public void mutateEvent(DeserializedEvent event) throws UnsupportedMutationException {
    Object payload = event.getPayload();

    if (payload == null) {
      return;
    }

    if (!(payload instanceof JsonObject)) {
      throw new UnsupportedMutationException("Payload data is not a JsonObject");
    }

    mutatePayload((JsonObject) payload);
  }
}
