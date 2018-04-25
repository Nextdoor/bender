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

package com.nextdoor.bender.operation.decode;

import java.util.Collections;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.operation.OperationConfig;

@JsonTypeName("UrlDecodeOperation")
@JsonSchemaDescription("Performs a url decode on the specified fields.")
public class URLDecodeOperationConfig extends OperationConfig {

  @JsonSchemaDescription("fields")
  @JsonSchemaDefault(value = "{}")
  @JsonProperty(required = true)
  private List<String> fields = Collections.emptyList();

  @JsonSchemaDescription("Number of times to perform url decode on field. This is helpful "
      + "if a field is encoded multiple times.")
  @JsonProperty(required = false)
  @JsonSchemaDefault(value = "1")
  @Min(1)
  @Max(65535)
  private Integer times = 1;

  public List<String> getFields() {
    return this.fields;
  }

  public void setFields(List<String> fields) {
    this.fields = fields;
  }

  public Integer getTimes() {
    return this.times;
  }

  public void setTimes(Integer times) {
    this.times = times;
  }

  @Override
  public Class<URLDecodeOperationFactory> getFactoryClass() {
    return URLDecodeOperationFactory.class;
  }
}
