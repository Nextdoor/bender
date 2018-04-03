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

package com.nextdoor.bender.operation.substitution;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;

public class SubstitutionSpec {
  @JsonSchemaDescription("Name of the new field")
  @JsonProperty(required = true)
  private String key;

  @JsonSchemaDescription("Value of the new field")
  @JsonProperty(required = true)
  private String value;

  @JsonSchemaDescription("Interpreter of the value. If FIELD then the field value will be pulled from the event.")
  @JsonProperty(required = true)
  @JsonSchemaDefault("STRING")
  private Interpreter interpreter = Interpreter.STATIC;

  public enum Interpreter {
    STATIC, FIELD
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getKey() {
    return this.key;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }

  public Interpreter getInterpreter() {
    return this.interpreter;
  }

  public SubstitutionSpec(String key, String value, Interpreter interpreter) {
    this.key = key;
    this.value = value;
    this.interpreter = interpreter;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SubstitutionSpec)) {
      return false;
    }

    SubstitutionSpec other = (SubstitutionSpec) o;

    if (this.key != other.getKey()) {
      return false;
    }

    if (this.value != other.getValue()) {
      return false;
    }

    if (this.interpreter != other.getInterpreter()) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.key, this.value, this.interpreter);
  }

  public String toString() {
    return this.key + ":" + this.value + ":" + this.interpreter.toString();
  }
}
