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

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nextdoor.bender.operation.json.PayloadOperation;

/**
 * In place recursively mutates a {@link JsonObject} keys to lower case them.
 */
public class LowerCaseKeyOperation extends PayloadOperation {
  protected void perform(JsonObject obj) {
    Set<Entry<String, JsonElement>> entries = obj.entrySet();
    Set<Entry<String, JsonElement>> orgEntries = new HashSet<>(entries);

    for (Entry<String, JsonElement> entry : orgEntries) {

      JsonElement val = entry.getValue();
      obj.remove(entry.getKey());
      String key = entry.getKey().toLowerCase();

      if (val.isJsonPrimitive()) {
        obj.add(key, val);
      } else if (val.isJsonObject()) {
        obj.add(key, val);
        perform(val.getAsJsonObject());
      } else if (val.isJsonArray()) {
        obj.add(key, val);

        val.getAsJsonArray().forEach(elm -> {
          if (elm.isJsonObject()) {
            perform((JsonObject) elm);
          }
        });
      }
    }
  }
}
