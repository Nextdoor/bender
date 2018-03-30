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

package com.nextdoor.bender.operation.gelf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.operation.Operation;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.operation.json.key.FlattenOperation;
import com.nextdoor.bender.operation.substitution.SubstitutionOperation;
import com.nextdoor.bender.operation.substitution.SubstitutionSpec;

public class GelfOperation implements Operation {

  private final FlattenOperation flattenOp;
  private final SubstitutionOperation subOp;

  private static final Set<String> GELF_FIELDS = new HashSet<>(Arrays.asList("version", "host",
      "short_message", "full_message", "timestamp", "level", "facility", "line", "file"));


  public GelfOperation(ArrayList<SubstitutionSpec> subSpecs) {
    this.subOp = new SubstitutionOperation(subSpecs);
    this.flattenOp = new FlattenOperation(".");
  }

  protected InternalEvent prefix(InternalEvent ievent) {
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

    JsonObject obj = (JsonObject) payload;

    Set<Entry<String, JsonElement>> entries = obj.entrySet();
    Set<Entry<String, JsonElement>> orgEntries = new HashSet<Entry<String, JsonElement>>(entries);

    /*
     * Prefix additional fields with "_". Everything that is not a GELF field is additional.
     */
    for (Entry<String, JsonElement> entry : orgEntries) {
      String key = entry.getKey();

      if (GELF_FIELDS.contains(key)) {
        continue;
      }

      JsonElement val = entry.getValue();
      obj.remove(key);

      obj.add("_" + key, val);
    }

    return ievent;
  }

  public InternalEvent perform(InternalEvent ievent) {
    /*
     * Substitute
     */
    ievent = subOp.perform(ievent);

    /*
     * Flatten
     */
    ievent = flattenOp.perform(ievent);

    /*
     * Prefix
     */
    ievent = prefix(ievent);

    return ievent;
  }

  public ArrayList<SubstitutionSpec> getSubSpecs() {
    return this.subOp.getSubSpecs();
  }
}