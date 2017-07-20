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

package com.nextdoor.bender.deserializer.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.deserializer.json.TimeSeriesJsonEvent.TimeFieldType;

@JsonTypeName("TimeSeriesJson")
public class TimeSeriesJsonDeserializerConfig extends AbstractJsonDeserializerConfig {

  @JsonSchemaDescription("Path to a JSON node which is used as a timestamp. See https://github.com/jayway/JsonPath")
  @JsonProperty(required = true)
  private String timeField;

  @JsonSchemaDescription("How to interpret time field")
  @JsonProperty(required = true)
  private TimeFieldType timeFieldType;

  @Override
  public Class getFactoryClass() {
    return TimeSeriesJsonDeserializerFactory.class;
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
