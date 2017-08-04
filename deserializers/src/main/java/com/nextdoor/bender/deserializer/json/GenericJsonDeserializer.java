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

package com.nextdoor.bender.deserializer.json;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.nextdoor.bender.deserializer.DeserializationException;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.Deserializer;
import com.nextdoor.bender.deserializer.json.AbstractJsonDeserializerConfig.FieldConfig;
import com.nextdoor.bender.partition.PartitionSpec;

/**
 * Converts a JSON string into a JsonElement object.
 */
public class GenericJsonDeserializer extends Deserializer {
  protected JsonParser parser;
  private final List<FieldConfig> nestedFieldConfigs;
  private String rootNodeOverridePath;

  public GenericJsonDeserializer(List<FieldConfig> nestedFieldConfigs) {
    this(nestedFieldConfigs, null);
  }

  public GenericJsonDeserializer(List<FieldConfig> nestedFieldConfigs, String rootNodeOverridePath) {
    this.nestedFieldConfigs = nestedFieldConfigs;
    this.rootNodeOverridePath = rootNodeOverridePath;
  }

  @Override
  public DeserializedEvent deserialize(String raw) {
    GenericJsonEvent devent = new GenericJsonEvent(null);

    JsonElement elm;
    try {
      elm = parser.parse(raw);
    } catch (JsonSyntaxException e) {
      throw new DeserializationException(e);
    }

    if (!elm.isJsonObject()) {
      throw new DeserializationException("event is not a json object");
    }

    JsonObject obj = elm.getAsJsonObject();

    /*
     * Convert fields which are nested json strings into json objects
     */
    for (FieldConfig fconfig : this.nestedFieldConfigs) {
      if (obj.has(fconfig.getField())) {
        JsonElement msg = obj.get(fconfig.getField());

        /*
         * Find the JSON in the string and replace the field
         */
        NestedData data = deserialize(msg);

        obj.remove(fconfig.getField());
        obj.add(fconfig.getField(), data.nested);

        /*
         * If the string contained data before the JSON store it in a new field
         */
        if (fconfig.getPrefixField() != null && data.prefix != null) {
          obj.add(fconfig.getPrefixField(), data.prefix);
        }
      }
    }

    if (rootNodeOverridePath != null) {
      obj = JsonPath.read(obj, rootNodeOverridePath);
      if (obj == null) {
        throw new DeserializationException(rootNodeOverridePath + " path not found in object");
      }
    }

    devent.setPayload(obj);

    return devent;
  }

  /**
   * Initializes the Gson JsonParser.
   */
  @Override
  public void init() {
    this.parser = new JsonParser();

    /*
     * Set static configuration for JsonPath
     */
    com.jayway.jsonpath.Configuration.setDefaults(new com.jayway.jsonpath.Configuration.Defaults() {
      private final JsonProvider jsonProvider = new GsonJsonProvider();
      private final MappingProvider mappingProvider = new GsonMappingProvider();

      @Override
      public JsonProvider jsonProvider() {
        return jsonProvider;
      }

      @Override
      public MappingProvider mappingProvider() {
        return mappingProvider;
      }

      @Override
      public Set<Option> options() {
        return EnumSet.of(Option.SUPPRESS_EXCEPTIONS);
      }
    });
  }

  /**
   * Stores the parsed string as a JSON object as well any prefix that may have proceeded the JSON
   * in the string.
   */
  protected static class NestedData {
    public JsonElement nested;
    public JsonPrimitive prefix;

    public NestedData(JsonElement nested) {
      this.nested = nested;
    }

    public NestedData(JsonElement nested, String prefix) {
      this.nested = nested;
      this.prefix = new JsonPrimitive(prefix);
    }
  }

  private NestedData deserialize(JsonElement json) throws JsonParseException {
    final String messageStr = json.getAsString();
    /*
     * Find what might be JSON
     */
    int braceIndex = messageStr.indexOf('{');

    if (braceIndex == -1) {
      return new NestedData(json);
    }

    String maybeNestedJson = messageStr.substring(braceIndex);

    /*
     * Attempt to deserialize what we think might be JSON
     */
    JsonElement nestedJson;
    try {
      nestedJson = parser.parse(maybeNestedJson);
    } catch (Exception e) {
      return new NestedData(json);
    }

    /*
     * Only JSON objects are supported for MESSAGE payloads. No primitives or arrays.
     */
    if (!nestedJson.isJsonObject()) {
      return new NestedData(json);
    } else {
      return new NestedData(nestedJson.getAsJsonObject(), messageStr.substring(0, braceIndex));
    }
  }
}
