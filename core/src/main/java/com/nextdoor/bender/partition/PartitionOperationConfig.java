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

package com.nextdoor.bender.partition;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.operation.OperationConfig;

@JsonTypeName("PartitionOperation")
@JsonSchemaDescription("Sets the partition information for the Event using fields from the "
    + "deserialized object. When using JSON use JsonPath format to specify fields. See "
    + "https://github.com/jayway/JsonPath")
public class PartitionOperationConfig extends OperationConfig {

  @JsonSchemaDescription("Configuration to specify object fields that are treated as partitions")
  @JsonSchemaDefault("[]")
  @JsonProperty(required = false)
  private List<PartitionSpec> partitionSpecs = Collections.emptyList();

  @JsonProperty("partition_specs")
  public List<PartitionSpec> getPartitionSpecs() {
    return partitionSpecs;
  }

  @JsonProperty("partition_specs")
  public void setPartitionSpecs(List<PartitionSpec> partitionSpecs) {
    this.partitionSpecs = partitionSpecs;
  }

  @Override
  public Class<PartitionOperationFactory> getFactoryClass() {
    return PartitionOperationFactory.class;
  }
}
