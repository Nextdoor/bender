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

package com.nextdoor.bender.operation.substitution;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;

@JsonTypeName("StaticSubstitution")
@JsonSchemaDescription("Substitutes event field value for a static value.")
public class StaticSubSpecConfig extends SubSpecConfig<StaticSubSpecConfig> {
  public StaticSubSpecConfig() {}

  public StaticSubSpecConfig(String key, String value, boolean failDstNotFound) {
    super(key, failDstNotFound);
    this.value = value;
  }

  @JsonSchemaDescription("Value of the new field.")
  @JsonProperty(required = true)
  private String value;

  public void setValue(String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }

  @Override
  public boolean equals(Object o) {
    if (!super.equals(o)) {
      return false;
    }

    if (!(o instanceof StaticSubSpecConfig)) {
      return false;
    }

    StaticSubSpecConfig other = (StaticSubSpecConfig) o;

    if (!this.value.equals(other.getValue())) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), this.value);
  }
}
