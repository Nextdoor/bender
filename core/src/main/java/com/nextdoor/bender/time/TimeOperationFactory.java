package com.nextdoor.bender.time;

import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.operation.OperationFactory;

public class TimeOperationFactory implements OperationFactory {
  private TimeOperationConfig config;
  
  @Override
  public void setConf(AbstractConfig config) {
    config = (TimeOperationConfig) config;
  }

  @Override
  public Class<TimeOperation> getChildClass() {
    return TimeOperation.class;
  }

  @Override
  public TimeOperation newInstance() {
    return new TimeOperation(config.getTimeField(), config.getTimeFieldType());
  }
}
