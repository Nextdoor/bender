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
  private String srcHostField;

  @JsonSchemaDescription("Field containing short_message string")
  @JsonProperty(required = true)
  private String srcShortMessageField;

  @JsonSchemaDescription("Field containing full_message string")
  @JsonProperty(required = false)
  private String srcFullMessageField;

  @JsonSchemaDescription("Field containing epoch timestamp in seconds with optional ms as decimal. Source "
      + "field must be in numeric form (not a string) or Graylog will reject the input. Alternatively, use "
      + "the TimeOperation to detect your timestamp field and properly parse it, and the GelfOperation will "
      + "automatically use that to set the timestamp field properly. Use this setting only to override that "
      + "behavior.")
  @JsonProperty(required = false)
  private String srcTimestampField;

  @JsonSchemaDescription("Field containing syslog level number")
  @JsonProperty(required = false)
  private String srcLevelField;

  @JsonSchemaDescription("Field containing syslog facility string")
  @JsonProperty(required = false)
  private String srcFacilityField;

  @JsonSchemaDescription("Field containing the line in a file that caused the error (decimal); optional, "
      + "deprecated. Send as additional field instead.")
  @JsonProperty(required = false)
  private String srcLineNumberField;

  @JsonSchemaDescription("Field containing the file (with path if you want) that caused the error "
      + "(string); optional, deprecated. Send as additional field instead.")
  @JsonProperty(required = false)
  private String srcFileField;

  public String getSrcHostField() {
    return this.srcHostField;
  }

  public void setSrcHostField(String srcHostField) {
    this.srcHostField = srcHostField;
  }

  public String getSrcShortMessageField() {
    return this.srcShortMessageField;
  }

  public void setSrcShortMessageField(String srcShortMessageField) {
    this.srcShortMessageField = srcShortMessageField;
  }


  public String getSrcFullMessageField() {
    return this.srcFullMessageField;
  }

  public void setSrcFullMessageField(String srcFullMessageField) {
    this.srcFullMessageField = srcFullMessageField;
  }

  public String getSrcTimestampField() {
    return this.srcTimestampField;
  }

  public void setSrcTimestampField(String srcTimestampField) {
    this.srcTimestampField = srcTimestampField;
  }

  public String getSrcLevelField() {
    return this.srcLevelField;
  }

  public void setSrcLevelField(String srcLevelField) {
    this.srcLevelField = srcLevelField;
  }

  public String getSrcFacilityField() {
    return this.srcFacilityField;
  }

  public void setSrcFacilityField(String srcFacilityField) {
    this.srcFacilityField = srcFacilityField;
  }

  public String getSrcLineNumberField() {
    return this.srcLineNumberField;
  }

  public void setSrcLineNumberField(String srcLineNumberField) {
    this.srcLineNumberField = srcLineNumberField;
  }

  public String getSrcFileField() {
    return this.srcFileField;
  }

  public void setSrcFileField(String srcFileField) {
    this.srcFileField = srcFileField;
  }

  @Override
  public Class<GelfOperationFactory> getFactoryClass() {
    return GelfOperationFactory.class;
  }
}
