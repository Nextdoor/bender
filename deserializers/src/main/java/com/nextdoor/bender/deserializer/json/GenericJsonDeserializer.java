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

package com.nextdoor.bender.deserializer.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.nextdoor.bender.deserializer.DeserializationException;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.Deserializer;
import com.nextdoor.bender.deserializer.json.GenericJsonDeserializerConfig.FieldConfig;
import org.apache.commons.io.IOUtils;

/**
 * Converts a JSON string into a JsonElement object.
 */
public class GenericJsonDeserializer extends Deserializer {
  protected JsonParser parser;
  private final List<FieldConfig> nestedFieldConfigs;
  private String rootNodeOverridePath;
  private Base64.Decoder base64decoder;
  private ByteArrayOutputStream byteArrayOutputStream;
  private final boolean performBase64DecodeAndUnzip;
  private final int bufferSize;

  public GenericJsonDeserializer(List<FieldConfig> nestedFieldConfigs) {
    this(nestedFieldConfigs, null, false, 1024);
  }

  public GenericJsonDeserializer(List<FieldConfig> nestedFieldConfigs,
                                 String rootNodeOverridePath,
                                 boolean performBase64DecodeAndUnzip,
                                 int bufferSize) {
    this.nestedFieldConfigs = nestedFieldConfigs;
    this.rootNodeOverridePath = rootNodeOverridePath;
    this.base64decoder = Base64.getDecoder();
    this.byteArrayOutputStream = new ByteArrayOutputStream();
    this.performBase64DecodeAndUnzip = performBase64DecodeAndUnzip;
    this.bufferSize = bufferSize;
  }

  public byte[] readGzipCompressedData(byte[] data) throws IOException {
    GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(data));
    IOUtils.copy(gzipInputStream, byteArrayOutputStream, bufferSize);
    try {
      return byteArrayOutputStream.toByteArray();
    } finally {
      byteArrayOutputStream.reset(); //clears output so it can be used again later
    }
  }

  @Override
  public DeserializedEvent deserialize(String raw) {
    GenericJsonEvent devent = new GenericJsonEvent(null);

    if (performBase64DecodeAndUnzip) {
      try {
        byte[] decoded = base64decoder.decode(raw);
        byte[] unzipped = readGzipCompressedData(decoded);
        raw = new String(unzipped);
      } catch (Exception e) {
        throw new DeserializationException(e);
      }
    }

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
      Object o = JsonPathProvider.read(obj, rootNodeOverridePath);
      if (obj == null || o instanceof JsonNull) {
        throw new DeserializationException(rootNodeOverridePath + " path not found in object");
      }
      obj = (JsonObject) o;
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
