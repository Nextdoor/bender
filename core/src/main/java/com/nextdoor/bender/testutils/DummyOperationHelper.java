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
 * Copyright $year Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.testutils;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.operation.Operation;
import com.nextdoor.bender.operation.OperationConfig;
import com.nextdoor.bender.operation.OperationFactory;

public class DummyOperationHelper {
  public static class DummyOperation implements Operation {
    @Override
    public InternalEvent perform(InternalEvent event) {
      return event;
    }
  }

  @JsonTypeName("DummyOperationHelper$DummyOperationConfig")
  public static class DummyOperationConfig extends OperationConfig {

    @Override
    public Class<DummyOperationFactory> getFactoryClass() {
      return DummyOperationFactory.class;
    }
  }

  public static class DummyOperationFactory implements OperationFactory {
    public Operation op;

    public DummyOperationFactory() {}

    public DummyOperationFactory(Operation op) {
      this.op = op;
    }

    @Override
    public Operation newInstance() {
      return this.op == null ? new DummyOperation() : this.op;
    }

    @Override
    public Class<DummyOperation> getChildClass() {
      return DummyOperation.class;
    }

    @Override
    public void setConf(AbstractConfig config) {}
  }
}
