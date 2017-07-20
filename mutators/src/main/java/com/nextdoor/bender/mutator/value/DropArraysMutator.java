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

package com.nextdoor.bender.mutator.value;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nextdoor.bender.mutator.PayloadMutator;

/**
 * In place recursively mutates a {@link JsonObject} values to drop arrays. This mutator can be used
 * when loading into Elasticsearch as it does not fully support an array of maps.
 */
public class DropArraysMutator extends PayloadMutator {
  protected void mutatePayload(JsonObject obj) {
    Set<Entry<String, JsonElement>> entries = obj.entrySet();
    Set<Entry<String, JsonElement>> orgEntries = new HashSet<Entry<String, JsonElement>>(entries);

    for (Entry<String, JsonElement> entry : orgEntries) {
      JsonElement val = entry.getValue();

      if (val.isJsonArray()) {
        obj.remove(entry.getKey());
      } else if (val.isJsonObject()) {
        mutatePayload(val.getAsJsonObject());
      }
    }
  }
}
