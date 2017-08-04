package com.nextdoor.bender.partition;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.operation.OperationConfig;

@JsonTypeName("PartitionOperation")
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
