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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nextdoor.bender.operation.json.PayloadOperation;

/**
 * In place recursively mutates a {@link JsonObject} to replace parts of a key with a value. This is
 * typically used to sanitize key names.
 */
public class KeyNameReplacementOperation extends PayloadOperation {
  public final Pattern pattern;
  public final String replacement;
  private final boolean drop;

  public KeyNameReplacementOperation(final Pattern pattern, String replacement, boolean drop) {
    this.pattern = pattern;
    this.replacement = replacement;
    this.drop = drop;
  }

  protected void performOnArray(JsonElement elm) {
    if (elm.isJsonObject()) {
      perform(elm.getAsJsonObject());
    } else if (elm.isJsonArray()) {
      JsonArray arr = elm.getAsJsonArray();
      arr.forEach(item -> {
        performOnArray(item);
      });
    }
  }

  protected void perform(JsonObject obj) {
    Set<Entry<String, JsonElement>> entries = obj.entrySet();
    Set<Entry<String, JsonElement>> orgEntries = new HashSet<Entry<String, JsonElement>>(entries);

    for (Entry<String, JsonElement> entry : orgEntries) {
      JsonElement val = entry.getValue();

      /*
       * See if key matches. If it does then drop or rename. Otherwise keep recursing.
       */
      Matcher m = this.pattern.matcher(entry.getKey());
      boolean found = m.find();
      if (found) {
        /*
         * If instructed to drop then remove and continue. Otherwise remove and later rename;
         */
        obj.remove(entry.getKey());
        if (this.drop) {
          continue;
        }

        /*
         * Rename
         */
        if (val.isJsonPrimitive()) {
          obj.add(m.replaceAll(this.replacement), val);
        } else if (val.isJsonObject()) {
          obj.add(m.replaceAll(this.replacement), val);
          perform(val.getAsJsonObject());
        } else if (val.isJsonArray()) {
          JsonArray arr = val.getAsJsonArray();

          arr.forEach(item -> {
            performOnArray(item);
          });
          obj.add(m.replaceAll(this.replacement), val);
        }
        continue;
      }

      /*
       * Keep recursing
       */
      if (val.isJsonObject()) {
        perform(val.getAsJsonObject());
      } else if (val.isJsonArray()) {
        JsonArray arr = val.getAsJsonArray();
        arr.forEach(item -> {
          performOnArray(item);
        });
      }

    }
  }
}
