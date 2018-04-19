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
package com.nextdoor.bender.operation.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.operation.OperationConfig;

@JsonTypeName("FilterOperation")
@JsonSchemaDescription("Provided an event, it will remove the event from the stream "
    + "depending on whether or not it contains a specified field matching a specified regex.")
public class FilterOperationConfig extends OperationConfig {

  @JsonSchemaDescription("Regex to be matched against JSON objects. See "
      + "https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html")
  @JsonProperty(required = true)
  private String regex = null;

  @JsonSchemaDescription("JsonPath to the element to be matched against. See "
      + "https://github.com/jayway/JsonPath")
  @JsonProperty(required = true)
  private String path;

  @JsonSchemaDescription("If true, matches will be filtered out. If false, non-matches will be "
      + "filtered out.")
  @JsonProperty(required = false)
  @JsonSchemaDefault(value = "false")
  private Boolean match = false;

  public String getRegex() {
    return regex;
  }

  public void setRegex(String regex) {
    this.regex = regex;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Boolean getMatch() {
    return match;
  }

  public void setMatch(Boolean match) {
    this.match = match;
  }

  @Override
  public Class<FilterOperationFactory> getFactoryClass() {
    return FilterOperationFactory.class;
  }
}
