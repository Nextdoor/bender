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

package com.nextdoor.bender.operation.json.key;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.operation.OperationConfig;

@JsonTypeName("KeyNameReplacementOperation")
@JsonSchemaDescription("Uses a regex to find matching parts of a key name and replaces with value "
    + "or drops the key/value if a match is found. This is typically used to sanitize key names.")
public class KeyNameReplacementOperationConfig extends OperationConfig {

  @JsonSchemaDescription("Java regex. See https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html")
  @JsonProperty(required = true)
  private String regex = null;

  @JsonSchemaDescription("If a match is found then replace match with this value. Defaults to emptry string.")
  @JsonProperty(required = false)
  @JsonSchemaDefault(value = "")
  private String replacement = "";

  @JsonSchemaDescription("If a match is found then drop the key/value.")
  @JsonProperty(required = false)
  @JsonSchemaDefault(value = "false")
  private Boolean drop = false;

  public String getRegex() {
    return this.regex;
  }

  public void setRegex(String regex) {
    this.regex = regex;
  }

  public String getReplacement() {
    return this.replacement;
  }

  public void setReplacement(String replacement) {
    this.replacement = replacement;
  }

  public Boolean getDrop() {
    return this.drop;
  }

  void setDrop(Boolean drop) {
    this.drop = drop;
  }

  @Override
  public Class<KeyNameReplacementOperationFactory> getFactoryClass() {
    return KeyNameReplacementOperationFactory.class;
  }
}
