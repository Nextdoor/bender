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

import java.util.EnumSet;
import java.util.Set;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.Predicate;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

/**
 * Statically initializes JsonPath and provides commonly used methods of JsonPath
 */
public class JsonPathProvider {
  /**
   * Setup JsonPath defaults
   */
  static {
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

  public static DocumentContext parse(String json) {
    return JsonPath.parse(json);
  }

  public static DocumentContext parse(Object json) {
    return JsonPath.parse(json);
  }

  public static <T> T read(Object json, String jsonPath, Predicate... filters) {
    return JsonPath.read(json, jsonPath, filters);
  }

  public static void delete(Object json, String jsonPath) {
    JsonPath.parse(json).delete(jsonPath);
  }
}
