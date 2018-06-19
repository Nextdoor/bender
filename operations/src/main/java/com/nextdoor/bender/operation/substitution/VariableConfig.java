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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.config.AbstractConfig;


public abstract class VariableConfig<T> extends AbstractConfig<T> {
  public VariableConfig() {};

  @JsonTypeName("FieldVariable")
  public static class FieldVariable extends VariableConfig<FieldVariable> {
    public FieldVariable() {};

    public FieldVariable(String variable, List<String> srcFields) {
      this.srcFields = srcFields;
    }

    @JsonSchemaDescription("Source fields to pull value from. If multiple fields are provided the "
        + "first non-null valued one is used. Note that if no fields are found the value will "
        + "be set to null.")
    @JsonProperty(required = true)
    private List<String> srcFields;

    @JsonSchemaDescription("Fail if source field is not found.")
    @JsonProperty(required = false)
    @JsonSchemaDefault(value = "true")
    private Boolean failSrcNotFound = true;

    @JsonSchemaDescription("Removes the source field when performing the substitution.")
    @JsonSchemaDefault(value = "false")
    @JsonProperty(required = false)
    private Boolean removeSrcField = false;

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

    public void setSrcFields(List<String> srcFields) {
      this.srcFields = srcFields;
    }

    public List<String> getSrcFields() {
      return this.srcFields;
    }
  }

  @JsonTypeName("StaticVariable")
  public static class StaticVariable extends VariableConfig<StaticVariable> {
    public StaticVariable() {};

    public StaticVariable(String value) {
      this.value = value;
    }

    @JsonSchemaDescription("Value to substitute.")
    @JsonProperty(required = true)
    private String value;

    public String getValue() {
      return this.value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }
}
