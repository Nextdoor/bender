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
 * Copyright 2018 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.operation.json.key;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nextdoor.bender.operation.json.PayloadOperation;

/**
 * Flattens out a nested JSON Object into a simple object with only one layer of key/values.
 */
public class FlattenOperation extends PayloadOperation {
    private final String separator;

    public FlattenOperation(String separator) {
        this.separator = separator;
    }

    protected void perform(JsonObject obj) {

        Set<Entry<String, JsonElement>> entries = obj.entrySet();
        Set<Entry<String, JsonElement>> orgEntries = new HashSet<>(entries);

        for (Entry<String, JsonElement> entry : orgEntries) {
            JsonElement val = entry.getValue();

            if (val.isJsonPrimitive() || val.isJsonNull()) {
                continue;
            }

            obj.remove(entry.getKey());
            if (val.isJsonObject()) {
                perform(obj, val.getAsJsonObject(), entry.getKey());
            } else if (val.isJsonArray()) {
                perform(obj, val.getAsJsonArray(), entry.getKey());
            }
        }
    }

    protected void perform(JsonObject obj, JsonArray nested_arr, String parent) {
        int c = 0;
        for (JsonElement val : nested_arr) {
            c += 1;

            String key = parent + separator + c;

            if (val.isJsonArray()) {
                perform(obj, val.getAsJsonArray(), key);
            } else if (val.isJsonObject()) {
                perform(obj, val.getAsJsonObject(), key);
            } else {
                obj.add(key, val);
            }
        }
    }

    protected void perform(JsonObject obj, JsonObject nested_obj, String parent) {
        Set<Entry<String, JsonElement>> entries = nested_obj.entrySet();
        Set<Entry<String, JsonElement>> orgEntries = new HashSet<>(entries);

        for (Entry<String, JsonElement> entry : orgEntries) {
            JsonElement val = entry.getValue();
            String key = parent + separator + entry.getKey();

            if (val.isJsonObject()) {
                perform(obj, val.getAsJsonObject(), key);
            } else if (val.isJsonArray()) {
                perform(obj, val.getAsJsonArray(), key);
            } else {
                obj.add(key, val);
            }
        }
    }
}
