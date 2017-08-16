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

package com.nextdoor.bender.deserializer.regex;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;

public class ReFieldConfig {
  @JsonSchemaDescription("Name to give to field")
  @JsonProperty(required = true)
  private String name;

  @JsonSchemaDescription("Data type of field")
  @JsonProperty(required = true)
  private ReFieldType type;

  public static enum ReFieldType {
    STRING, NUMBER, BOOLEAN
  }

  public ReFieldConfig() {

  }

  public ReFieldConfig(String name, ReFieldType type) {
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ReFieldType getType() {
    return type;
  }

  public void setType(ReFieldType type) {
    this.type = type;
  }
}
