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

package com.nextdoor.bender.operation.fork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.operation.OperationConfig;
import com.nextdoor.bender.operation.OperationFactory;
import com.nextdoor.bender.operation.OperationFactoryFactory;
import com.nextdoor.bender.operation.OperationProcessor;
import com.nextdoor.bender.operation.StreamOperation;
import com.nextdoor.bender.operation.fork.ForkOperationConfig.Fork;

public class ForkOperationFactory implements OperationFactory {
  private ForkOperationConfig config;
  private List<List<OperationProcessor>> processors = Collections.emptyList();

  @Override
  public void setConf(AbstractConfig config) {
    this.config = (ForkOperationConfig) config;

    List<List<OperationProcessor>> processors = new ArrayList<List<OperationProcessor>>();
    OperationFactoryFactory off = new OperationFactoryFactory();

    for (Fork fork : this.config.getForks()) {
      List<OperationProcessor> processorsInFork = new ArrayList<OperationProcessor>();

      for (OperationConfig opConfig : fork.getOperations()) {
        try {
          processorsInFork.add(new OperationProcessor(off.getFactory(opConfig)));
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      }

      processors.add(processorsInFork);
    }

    this.processors = processors;
  }

  @Override
  public Class<?> getChildClass() {
    return ForkOperation.class;
  }

  @Override
  public StreamOperation newInstance() {
    return new ForkOperation(this.processors);
  }
}
