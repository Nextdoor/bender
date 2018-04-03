/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * Copyright 2018 Nextdoor.com, Inc
 */

package com.nextdoor.bender.operation.json.key;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.nextdoor.bender.operation.json.PayloadOperation;

/**
 * In place recursively mutates a {@link JsonObject} keys to contain a suffix of the data type of
 * the associated field value. Also replaces "." with "_" in keys.
 */
public class KeyNameOperation extends PayloadOperation {
  protected void perform(JsonObject obj) {
    Set<Entry<String, JsonElement>> entries = obj.entrySet();
    Set<Entry<String, JsonElement>> orgEntries = new HashSet<Entry<String, JsonElement>>(entries);

    for (Entry<String, JsonElement> entry : orgEntries) {

      JsonElement val = entry.getValue();
      obj.remove(entry.getKey());
      String key = entry.getKey().toLowerCase().replaceAll("[ .]", "_");

      if (val.isJsonPrimitive()) {
        JsonPrimitive prim = val.getAsJsonPrimitive();

        if (prim.isBoolean()) {
          obj.add(key + "__bool", val);
        } else if (prim.isNumber()) {
          if (prim.toString().contains(".")) {
            obj.add(key + "__float", val);
          } else {
            obj.add(key + "__long", val);
          }
        } else if (prim.isString()) {
          obj.add(key + "__str", val);
        }
      } else if (val.isJsonObject()) {
        obj.add(key, val);
        perform(val.getAsJsonObject());
      } else if (val.isJsonArray()) {
        obj.add(key + "__arr", val);
      }
    }
  }
}
