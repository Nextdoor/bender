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

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.operation.FilterOperationConfig;

@JsonTypeName("BasicFilterOperation")
@JsonSchemaDescription("If pass is set to false then events are filtered out.")
public class BasicFilterOperationConfig extends FilterOperationConfig {

  @JsonSchemaDescription("If 'false', event is filtered out.")
  @JsonSchemaDefault(value = "false")
  private Boolean pass = false;

  public Boolean getPass() {
    return this.pass;
  }

  public void setPass(Boolean pass) {
    this.pass = pass;
  }

  @Override
  public Class<BasicFilterOperationFactory> getFactoryClass() {
    return BasicFilterOperationFactory.class;
  }
}
