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

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;

@JsonTypeName("FieldSubstitution")
@JsonSchemaDescription("Substitutes event field value for another event field value. Note the source "
    + "field and destination field can be the same.")
public class FieldSubSpecConfig extends SubSpecConfig<FieldSubSpecConfig> {
  public FieldSubSpecConfig(String key, List<String> sourceField) {
    super(key);
    this.sourceField = sourceField;
  }

  @JsonSchemaDescription("Source fields to pull value from. If multiple are specified first non-null "
      + "one is used. If multiple fields are provided the first non-null valued one is used. Note "
      + "that if no fields are found the value will be set to null.")
  @JsonProperty(required = true)
  private List<String> sourceField;

  public void setSourceFields(List<String> sourceField) {
    this.sourceField = sourceField;
  }

  public List<String> getSourceFields() {
    return this.sourceField;
  }

  @Override
  public boolean equals(Object o) {
    if (!super.equals(o)) {
      return false;
    }

    if (!(o instanceof FieldSubSpecConfig)) {
      return false;
    }

    FieldSubSpecConfig other = (FieldSubSpecConfig) o;

    if (this.sourceField != other.getSourceFields()) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), this.sourceField);
  }
}
