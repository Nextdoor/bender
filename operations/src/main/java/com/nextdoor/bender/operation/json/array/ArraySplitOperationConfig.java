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
 * Copyright 2017 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.operation.json.array;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.operation.OperationConfig;

@JsonTypeName("JsonArraySplitOperation")
@JsonSchemaDescription("Provided a path to a JSON array it will produce new events with payloads "
    + "coorepsonding to elements of the array. For example [{\"foo\": 1}, {\"bar\": 2}] will be "
    + "turned into two seperate events with payloads of {\"foo\": 1} and {\"bar\": 2}.")
public class ArraySplitOperationConfig extends OperationConfig {

  @JsonSchemaDescription("Path to a JSON node which is an array. See https://github.com/jayway/JsonPath")
  @JsonProperty(required = true)
  private String path;


  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public Class<ArraySplitOperationFactory> getFactoryClass() {
    return ArraySplitOperationFactory.class;
  }
}
