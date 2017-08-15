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

package com.nextdoor.bender.serializer.json;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.gson.FieldNamingPolicy;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.serializer.SerializerConfig;

@JsonTypeName("Json")
@JsonSchemaDescription("Serializes events into JSON format.")
public class JsonSerializerConfig extends SerializerConfig {
  @JsonSchemaDefault("LOWER_CASE_WITH_UNDERSCORES")
  @JsonSchemaDescription("GSON field naming policy. See https://google.github.io/gson/apidocs/com/google/gson/FieldNamingPolicy.html")
  private FieldNamingPolicy fieldNamingPolicy = FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;

  @Override
  public Class<JsonSerializerFactory> getFactoryClass() {
    return JsonSerializerFactory.class;
  }

  public FieldNamingPolicy getFieldNamingPolicy() {
    return fieldNamingPolicy;
  }

  public void setFieldNamingPolicy(FieldNamingPolicy fieldNamingPolicy) {
    this.fieldNamingPolicy = fieldNamingPolicy;
  }
}
