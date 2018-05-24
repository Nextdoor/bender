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
import com.nextdoor.bender.operation.FilterOperationConfig;

@JsonTypeName("RegexFilterOperation")
@JsonSchemaDescription("This operation is used to remove certain events from the stream before "
    + "continuing on to the destination. Each event is assessed by applying a JsonPath to its "
    + "payload and matching the value against a regex Pattern. If exclude is true, events that "
    + "match this criteria will be filtered out. If exclude is false, any events not matching this "
    + "criteria will be filtered out. For example, say these two events are in the stream: "
    + "{\\\"data\\\": \\\"one\\\", \\\"type\\\": \\\"bar\\\"} and "
    + "{\\\"data\\\": \\\"one\\\", \\\"type\\\": \\\"baz\\\"}. With config  values: "
    + "regex = \\\"(bar)\\\", path = \\\"$.type\\\", and exclude = true, after filtering, the "
    + "stream will only hold {\\\"data\\\": \\\"one\\\", \\\"type\\\": \\\"baz\\\"}. If instead, "
    + "exclude = false, the stream would only hold "
    + "{\\\"data\\\": \\\"one\\\", \\\"type\\\": \\\"bar\\\"} after filtering.")
public class RegexFilterOperationConfig extends FilterOperationConfig {

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
  @JsonSchemaDefault(value = "true")
  private Boolean exclude = true;

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

  public Boolean getExclude() {
    return this.exclude;
  }

  public void setExclude(Boolean exclude) {
    this.exclude = exclude;
  }

  @Override
  public Class<RegexFilterOperationFactory> getFactoryClass() {
    return RegexFilterOperationFactory.class;
  }
}
