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

package com.nextdoor.bender.testutils;

import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.operation.BaseOperation;
import com.nextdoor.bender.operation.EventOperation;
import com.nextdoor.bender.operation.OperationConfig;
import com.nextdoor.bender.operation.OperationFactory;

public class DummyThrottleOperationHelper {
  public static class DummyThrottleOperation implements EventOperation {

    public AtomicInteger counter = new AtomicInteger(0);

    @Override
    public InternalEvent perform(InternalEvent event) {
      if (this.counter.getAndIncrement() % 100 == 0) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
        }
      }
      return event;
    }
  }

  @JsonTypeName("DummyThrottleOperationHelper$DummyThrottleOperationConfig")
  public static class DummyThrottleOperationConfig extends OperationConfig {

    @Override
    public Class<DummyThrottleOperationFactory> getFactoryClass() {
      return DummyThrottleOperationFactory.class;
    }
  }

  public static class DummyThrottleOperationFactory implements OperationFactory {
    public BaseOperation op;

    public DummyThrottleOperationFactory() {}

    public DummyThrottleOperationFactory(BaseOperation op) {
      this.op = op;
    }

    @Override
    public BaseOperation newInstance() {
      return this.op == null ? new DummyThrottleOperation() : this.op;
    }

    @Override
    public Class<DummyThrottleOperation> getChildClass() {
      return DummyThrottleOperation.class;
    }

    @Override
    public void setConf(AbstractConfig config) {}
  }
}
