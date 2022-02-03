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

package com.nextdoor.bender.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.deserializer.DeserializerConfig;
import com.nextdoor.bender.operation.OperationConfig;

public class SourceConfig {
  @JsonSchemaDescription("Source name")
  @JsonProperty(required = true)
  private String name;

  @JsonSchemaDescription("Pattern to match source trigger against")
  @JsonSchemaDefault(value = ".*")
  @JsonProperty(required = false)
  private String sourceRegex = ".*";

  @JsonSchemaDescription("Deserializer configuration")
  private DeserializerConfig deserializerConfig;

  @JsonSchemaDescription("Operation configuration")
  @JsonProperty(required = false)
  private List<OperationConfig> operationConfigs = new ArrayList<>(0);

  @JsonSchemaDescription("Regex patterns to filter events by prior to deserialization")
  @JsonProperty(required = false)
  private List<String> regexPatterns = Collections.emptyList();

  @JsonSchemaDescription("Filter events containing these Strings prior to deserialization")
  @JsonProperty(required = false)
  private List<String> containsStrings = Collections.emptyList();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSourceRegex() {
    return sourceRegex;
  }

  public void setSourceRegex(String sourceRegex) {
    this.sourceRegex = sourceRegex;
  }

  @JsonProperty("deserializer")
  public DeserializerConfig getDeserializerConfig() {
    return deserializerConfig;
  }

  @JsonProperty("deserializer")
  public void setDeserializerConfig(DeserializerConfig deserializerConfig) {
    this.deserializerConfig = deserializerConfig;
  }

  @JsonProperty("operations")
  public List<OperationConfig> getOperationConfigs() {
    return this.operationConfigs;
  }

  @JsonProperty("operations")
  public void setOperationConfigs(List<OperationConfig> operationConfigs) {
    this.operationConfigs = operationConfigs;
  }

  public List<String> getRegexPatterns() {
    return regexPatterns;
  }

  public void setRegexPatterns(List<String> regexPatterns) {
    this.regexPatterns = regexPatterns;
  }

  public List<String> getContainsStrings() {
    return containsStrings;
  }

  public void setContainsStrings(List<String> containsStrings) {
    this.containsStrings = containsStrings;
  }
}
