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
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;

@JsonTypeName("FieldSubstitution")
@JsonSchemaDescription("Substitutes event field value for another event field value. Note the source "
    + "field and destination field can be the same.")
public class FieldSubSpecConfig extends SubSpecConfig<FieldSubSpecConfig> {
  public FieldSubSpecConfig() {}

  public FieldSubSpecConfig(String key, List<String> srcField, boolean removeSourceField, boolean failSrcNotFound, boolean failDstNotFound) {
    super(key, failDstNotFound);
    this.srcField = srcField;
    this.removeSrcField = removeSourceField;
    this.failSrcNotFound = failSrcNotFound;
  }

  @JsonSchemaDescription("Source fields to pull value from. If multiple fields are provided the "
      + "first non-null valued one is used. Note that if no fields are found the value will "
      + "be set to null.")
  @JsonProperty(required = true)
  private List<String> srcField;

  @JsonSchemaDescription("Removes the source field when performing the substitution. Effectively "
      + "making this a move operation.")
  @JsonSchemaDefault(value = "false")
  @JsonProperty(required = false)
  private Boolean removeSrcField = false;

  @JsonSchemaDescription("Fail if source field is not found.")
  @JsonProperty(required = false)
  @JsonSchemaDefault(value = "true")
  private Boolean failSrcNotFound = true;

  public void setSrcFields(List<String> srcField) {
    this.srcField = srcField;
  }

  public List<String> getSrcFields() {
    return this.srcField;
  }

  public Boolean getRemoveSrcField() {
    return this.removeSrcField;
  }

  public void setRemoveSrcField(Boolean removeSrcField) {
    this.removeSrcField = removeSrcField;
  }

  public Boolean getFailSrcNotFound() {
    return this.failSrcNotFound;
  }

  public void setFailSrcNotFound(Boolean failSrcNotFound) {
    this.failSrcNotFound = failSrcNotFound;
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

    if (!this.srcField.equals(other.getSrcFields())) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), this.srcField);
  }

  @Override
  public String toString() {
    return super.getKey() + ":" + this.getSrcFields();
  }
}
