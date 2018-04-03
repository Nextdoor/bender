/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * Copyright 2018 Nextdoor.com, Inc
 */

package com.nextdoor.bender.deserializer.regex;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.deserializer.DeserializerConfig;

@JsonTypeName("Regex")
@JsonSchemaDescription("Extracts fields from a line with regex groups. Note that number of groups "
    + "in regex must match number of fields specified.")
public class RegexDeserializerConfig extends DeserializerConfig {

  @JsonSchemaDescription("List of field names and their types")
  @JsonProperty(required = true)
  private List<ReFieldConfig> fields = Collections.emptyList();

  @JsonSchemaDescription("Java regex. See https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html")
  @JsonProperty(required = true)
  private String regex = null;

  @JsonSchemaDescription("Use the faster but less flexible regex library. See https://github.com/google/re2j")
  @JsonProperty(required = false)
  @JsonSchemaDefault(value = "false")
  private Boolean useRe2j = false;

  public List<ReFieldConfig> getFields() {
    return this.fields;
  }

  public void setFields(List<ReFieldConfig> fields) {
    this.fields = fields;
  }

  public String getRegex() {
    return this.regex;
  }

  public void setRegex(String regex) {
    this.regex = regex;
  }

  public void setUseRe2j(Boolean useRe2j) {
    this.useRe2j = useRe2j;
  }

  public Boolean isUseRe2j() {
    return this.useRe2j;
  }

  @Override
  public Class getFactoryClass() {
    return RegexDeserializerFactory.class;
  }
}
