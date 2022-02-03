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

package com.nextdoor.bender.operation.conditional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.operation.FilterOperation;
import com.nextdoor.bender.operation.OperationConfig;
import com.nextdoor.bender.operation.OperationFactory;
import com.nextdoor.bender.operation.OperationFactoryFactory;
import com.nextdoor.bender.operation.OperationProcessor;
import com.nextdoor.bender.operation.StreamOperation;
import com.nextdoor.bender.operation.conditional.ConditionalOperationConfig.Condition;
import com.nextdoor.bender.operation.fork.ForkOperation;

public class ConditionalOperationFactory implements OperationFactory {
  private ConditionalOperationConfig config;
  private List<Pair<FilterOperation, List<OperationProcessor>>> cases = Collections.emptyList();

  @Override
  public void setConf(AbstractConfig config) {
    this.config = (ConditionalOperationConfig) config;

    List<Pair<FilterOperation, List<OperationProcessor>>> cases =
        new ArrayList<>();
    OperationFactoryFactory off = new OperationFactoryFactory();

    for (Condition caze : this.config.getConditions()) {
      List<OperationProcessor> processorsInCase = new ArrayList<>();

      /*
       * Create {@OperationProcessor}s from configs
       */
      for (OperationConfig opConfig : caze.getOperations()) {
        try {
          processorsInCase.add(new OperationProcessor(off.getFactory(opConfig)));
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      }

      FilterOperation filter;
      try {
        filter = (FilterOperation) off.getFactory(caze.getCondition()).newInstance();
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }

      cases.add(
          new ImmutablePair<>(filter, processorsInCase));
    }

    this.cases = cases;
  }

  @Override
  public Class<?> getChildClass() {
    return ForkOperation.class;
  }

  @Override
  public StreamOperation newInstance() {
    return new ConditionalOperation(this.cases, this.config.getFilterNonMatch());
  }
}
