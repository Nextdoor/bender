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

package com.nextdoor.bender.operation.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.operation.OperationConfig;

@JsonTypeName("MetadataOperation")
@JsonSchemaDescription("Creates a new key inside your payload with the Bender runtime metadata "
    + "for an invocation of the code. This typically includes data about the source handler "
    + "(ie, KinesisHandler, S3Handler, etc) and metadata about how and when the function was "
    + "triggered")
public class MetadataOperationConfig extends OperationConfig {

  @JsonSchemaDescription("The name of the root-level key that you want to add to your object. "
      + "If you customize this, you must understand the type of object that you are working "
      + "with.")
  @JsonProperty(required = true)
  private String fieldName;

  @Override
  public Class<MetadataOperationFactory> getFactoryClass() {
    return MetadataOperationFactory.class;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }
}
