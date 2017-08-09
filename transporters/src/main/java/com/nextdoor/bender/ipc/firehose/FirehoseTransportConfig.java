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

package com.nextdoor.bender.ipc.firehose;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;

import com.nextdoor.bender.ipc.TransportConfig;
import com.nextdoor.bender.ipc.firehose.FirehoseTransportFactory.FirehoseBuffer;

@JsonTypeName("Firehose")
public class FirehoseTransportConfig extends TransportConfig {

  @JsonSchemaDescription("Firehose stream name to publish to")
  @JsonProperty(required = true)
  private String streamName;

  @JsonSchemaDescription("Type of buffer to use. BATCH buffer combines records "
      + "while SIMPLE does not. Use SIMPLE when your firehose writes to ElasticSearch.")
  @JsonProperty(required = false)
  @JsonSchemaDefault(value = "BATCH")
  private FirehoseBuffer firehoseBuffer = FirehoseBuffer.BATCH;

  @JsonSchemaDescription("If a new line should be appended to records. Use this when "
      + "selecting BATCH buffer.")
  @JsonSchemaDefault(value = "true")
  @JsonProperty(required = false)
  private Boolean appendNewline = true;

  public String getStreamName() {
    return streamName;
  }

  public void setStreamName(String streamName) {
    this.streamName = streamName;
  }

  public FirehoseBuffer getFirehoseBuffer() {
    return firehoseBuffer;
  }

  public void setFirehoseBuffer(FirehoseBuffer firehoseBuffer) {
    this.firehoseBuffer = firehoseBuffer;
  }

  public Boolean getAppendNewline() {
    return this.appendNewline;
  }

  public void setAppendNewline(Boolean appendNewline) {
    this.appendNewline = appendNewline;
  }

  @Override
  public Class<FirehoseTransportFactory> getFactoryClass() {
    return FirehoseTransportFactory.class;
  }
}
