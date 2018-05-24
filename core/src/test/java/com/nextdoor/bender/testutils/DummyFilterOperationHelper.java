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
import com.nextdoor.bender.operation.FilterOperation;
import com.nextdoor.bender.operation.FilterOperationConfig;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.operation.OperationFactory;


/**
 * The DummyFilterOperation helps test filtering within forked or conditional operations. Every
 * other event will be filtered out.
 */
public class DummyFilterOperationHelper {
  public static class DummyFilterOperation implements FilterOperation {

    public AtomicInteger counter = new AtomicInteger(0);

    @Override
    public boolean test(InternalEvent ievent) throws OperationException {
      
      if (counter.incrementAndGet() % 2 == 0) {
        return true;
      }

      return false;
    }
  }

  @JsonTypeName("DummyFilterOperationHelper$DummyFilterOperationConfig")
  public static class DummyFilterOperationConfig extends FilterOperationConfig {

    @Override
    public Class<DummyFilterOperationFactory> getFactoryClass() {
      return DummyFilterOperationFactory.class;
    }
  }

  public static class DummyFilterOperationFactory implements OperationFactory {
    public BaseOperation op;

    public DummyFilterOperationFactory() {}

    public DummyFilterOperationFactory(BaseOperation op) {
      this.op = op;
    }

    @Override
    public BaseOperation newInstance() {
      return this.op == null ? new DummyFilterOperation() : this.op;
    }

    @Override
    public Class<DummyFilterOperation> getChildClass() {
      return DummyFilterOperation.class;
    }

    @Override
    public void setConf(AbstractConfig config) {}
  }
}
