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

package com.nextdoor.bender.ipc.kinesis;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.ipc.RegionalTransportConfig;

@JsonTypeName("Kinesis")
@JsonSchemaDescription("Transports events to AWS Kinesis.")
public class KinesisTransportConfig extends RegionalTransportConfig {

  @JsonSchemaDescription("Kinesis stream name to publish to.")
  @JsonProperty(required = true)
  private String streamName;

  @JsonSchemaDescription("Maximum number records per put to Kinesis.")
  @JsonSchemaDefault(value = "500")
  @JsonProperty(required = false)
  @Min(1)
  @Max(500)
  private Integer batchSize = 500;

  public String getStreamName() {
    return streamName;
  }

  public void setStreamName(String streamName) {
    this.streamName = streamName;
  }

  public Integer getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(Integer batchSize) {
    this.batchSize = batchSize;
  }

  @Override
  public Class<KinesisTransportFactory> getFactoryClass() {
    return KinesisTransportFactory.class;
  }
}
