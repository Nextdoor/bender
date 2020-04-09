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

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.deserializer.DeserializerConfig;

@JsonTypeName("GenericJson")
@JsonSchemaDescription("Deserializes JSON without performing schema validation.")
public class GenericJsonDeserializerConfig extends DeserializerConfig {
  @JsonSchemaDescription("Configuration on how to interpret string fields which can also be "
      + "deserialized as JSON. For example: {\"foo\": \"{\"bar\": \"baz\"}\"} will become "
      + "{\"foo\": {\"bar\": \"baz\"}\"}.")
  @JsonProperty(required = false)
  private List<FieldConfig> nestedFieldConfigs = Collections.emptyList();

  @JsonSchemaDescription("When true the deserializer will assume that the raw event strings are base64 encoded and will"
          + "attempt to decode them and then expand the gzip file.")
  @JsonSchemaDefault(value = "false")
  @JsonProperty(required = false)
  private Boolean performBase64DecodeAndExpandGzip = false;

  @JsonSchemaDescription("This sets the buffer size (default 1024) when an event object is a gzip and needs to be expanded.")
  @JsonProperty(required = false)
  private Integer bufferSize = 1024;

  @JsonSchemaDescription("Path to a JSON node which is promoted to root node. See https://github.com/jayway/JsonPath")
  @JsonProperty(required = false)
  private String rootNodeOverridePath;

  public static class FieldConfig {
    @JsonSchemaDescription("String field which contains a JSON object.")
    @JsonProperty(required = true)
    private String field;

    @JsonSchemaDescription("Field to put any data which preceded JSON object.")
    @JsonProperty(required = false)
    private String prefixField;

    public String getField() {
      return field;
    }

    public void setField(String field) {
      this.field = field;
    }

    public String getPrefixField() {
      return prefixField;
    }

    public void setPrefixField(String prefixField) {
      this.prefixField = prefixField;
    }
  }

  public List<FieldConfig> getNestedFieldConfigs() {
    return nestedFieldConfigs;
  }

  public void setNestedFieldConfigs(List<FieldConfig> nestedFieldConfigs) {
    this.nestedFieldConfigs = nestedFieldConfigs;
  }

  public String getRootNodeOverridePath() {
    return rootNodeOverridePath;
  }

  public void setRootNodeOverridePath(String rootNodeOverridePath) {
    this.rootNodeOverridePath = rootNodeOverridePath;
  }

  public Boolean getPerformBase64DecodeAndExpandGzip() {
    return performBase64DecodeAndExpandGzip;
  }

  public void setPerformBase64DecodeAndExpandGzip(Boolean performBase64DecodeAndExpandGzip) {
    this.performBase64DecodeAndExpandGzip = performBase64DecodeAndExpandGzip;
  }

  public Integer getBufferSize() {
    return bufferSize;
  }

  public void setBufferSize(Integer bufferSize) {
    this.bufferSize = bufferSize;
  }

  @Override
  public Class<GenericJsonDeserializerFactory> getFactoryClass() {
    return GenericJsonDeserializerFactory.class;
  }
}
