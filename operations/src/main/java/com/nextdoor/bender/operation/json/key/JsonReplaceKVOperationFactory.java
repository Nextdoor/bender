package com.nextdoor.bender.operation.json.key;

import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.operation.BaseOperation;
import com.nextdoor.bender.operation.OperationFactory;

public class JsonReplaceKVOperationFactory implements OperationFactory {

  @Override
  public void setConf(AbstractConfig config) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Class<?> getChildClass() {
    // TODO Auto-generated method stub
    return JsonReplaceKVOperation.class;
  }

  @Override
  public BaseOperation newInstance() {
    return new JsonReplaceKVOperation("", "");
  }

}
