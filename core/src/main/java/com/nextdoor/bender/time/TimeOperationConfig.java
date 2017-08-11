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

package com.nextdoor.bender.time;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.operation.OperationConfig;

@JsonTypeName("TimeOperation")
@JsonSchemaDescription("Sets the Event timestamp using the provided field in the deserialized "
    + "object. When using JSON use JsonPath format to specify field. See "
    + "https://github.com/jayway/JsonPath")
public class TimeOperationConfig extends OperationConfig {

  public static enum TimeFieldType {
    SECONDS, MILLISECONDS
  }

  @JsonSchemaDescription("Name of field to use as time field")
  @JsonProperty(required = true)
  private String timeField;

  @JsonSchemaDescription("How to interpret time field")
  @JsonProperty(required = true)
  private TimeFieldType timeFieldType;

  @Override
  public Class<TimeOperationFactory> getFactoryClass() {
    return TimeOperationFactory.class;
  }

  public String getTimeField() {
    return timeField;
  }

  public void setTimeField(String timeField) {
    this.timeField = timeField;
  }

  public TimeFieldType getTimeFieldType() {
    return timeFieldType;
  }

  public void setTimeFieldType(TimeFieldType timeFieldType) {
    this.timeFieldType = timeFieldType;
  }
}
