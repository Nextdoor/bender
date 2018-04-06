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

package com.nextdoor.bender.operation.gelf;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.operation.OperationConfig;

@JsonTypeName("GelfOperation")
@JsonSchemaDescription("Transforms JSON input into a GELF message format "
    + "(See http://docs.graylog.org/en/2.4/pages/gelf.html details). Note that when "
    + "selecting source fields use the JsonPath notation https://github.com/json-path/JsonPath.")
public class GelfOperationConfig extends OperationConfig {
  @JsonSchemaDescription("Field containing host string")
  @JsonProperty(required = true)
  private List<String> srcHostField;

  @JsonSchemaDescription("Field containing short_message string")
  @JsonProperty(required = true)
  private List<String> srcShortMessageField;

  @JsonSchemaDescription("Field containing full_message string")
  @JsonProperty(required = false)
  private List<String> srcFullMessageField;

  @JsonSchemaDescription("Field containing epoch timestamp in seconds with optional ms as decimal. Source "
      + "field must be in numeric form (not a string) or Graylog will reject the input. Alternatively, use "
      + "the TimeOperation to detect your timestamp field and properly parse it, and the GelfOperation will "
      + "automatically use that to set the timestamp field properly. Use this setting only to override that "
      + "behavior.")
  @JsonProperty(required = false)
  private List<String> srcTimestampField;

  @JsonSchemaDescription("Field containing syslog level number")
  @JsonProperty(required = false)
  private List<String> srcLevelField;

  @JsonSchemaDescription("Field containing syslog facility string")
  @JsonProperty(required = false)
  private List<String> srcFacilityField;

  @JsonSchemaDescription("Field containing the line in a file that caused the error (decimal); optional, "
      + "deprecated. Send as additional field instead.")
  @JsonProperty(required = false)
  private List<String> srcLineNumberField;

  @JsonSchemaDescription("Field containing the file (with path if you want) that caused the error "
      + "(string); optional, deprecated. Send as additional field instead.")
  @JsonProperty(required = false)
  private List<String> srcFileField;

  public List<String> getSrcHostField() {
    return this.srcHostField;
  }

  public void setSrcHostField(List<String> srcHostField) {
    this.srcHostField = srcHostField;
  }

  public List<String> getSrcShortMessageField() {
    return this.srcShortMessageField;
  }

  public void setSrcShortMessageField(List<String> srcShortMessageField) {
    this.srcShortMessageField = srcShortMessageField;
  }

  public List<String> getSrcFullMessageField() {
    return this.srcFullMessageField;
  }

  public void setSrcFullMessageField(List<String> srcFullMessageField) {
    this.srcFullMessageField = srcFullMessageField;
  }

  public List<String> getSrcTimestampField() {
    return this.srcTimestampField;
  }

  public void setSrcTimestampField(List<String> srcTimestampField) {
    this.srcTimestampField = srcTimestampField;
  }

  public List<String> getSrcLevelField() {
    return this.srcLevelField;
  }

  public void setSrcLevelField(List<String> srcLevelField) {
    this.srcLevelField = srcLevelField;
  }

  public List<String> getSrcFacilityField() {
    return this.srcFacilityField;
  }

  public void setSrcFacilityField(List<String> srcFacilityField) {
    this.srcFacilityField = srcFacilityField;
  }

  public List<String> getSrcLineNumberField() {
    return this.srcLineNumberField;
  }

  public void setSrcLineNumberField(List<String> srcLineNumberField) {
    this.srcLineNumberField = srcLineNumberField;
  }

  public List<String> getSrcFileField() {
    return this.srcFileField;
  }

  public void setSrcFileField(List<String> srcFileField) {
    this.srcFileField = srcFileField;
  }

  @Override
  public Class<GelfOperationFactory> getFactoryClass() {
    return GelfOperationFactory.class;
  }
}
