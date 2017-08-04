package com.nextdoor.bender.time;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.operation.OperationConfig;

@JsonTypeName("TimeOperation")
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
