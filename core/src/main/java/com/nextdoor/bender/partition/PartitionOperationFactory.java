package com.nextdoor.bender.partition;

import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.operation.OperationFactory;

public class PartitionOperationFactory implements OperationFactory {
  private PartitionOperationConfig config;
  
  @Override
  public void setConf(AbstractConfig config) {
    config = (PartitionOperationConfig) config;
  }

  @Override
  public Class<PartitionOperation> getChildClass() {
    return PartitionOperation.class;
  }

  @Override
  public PartitionOperation newInstance() {
    return new PartitionOperation(config.getPartitionSpecs());
  }
}
