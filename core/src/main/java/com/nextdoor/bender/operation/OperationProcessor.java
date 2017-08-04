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

package com.nextdoor.bender.operation;

import java.util.stream.Stream;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.monitoring.MonitoredProcess;

public class OperationProcessor extends MonitoredProcess {
  private BaseOperation op;

  public OperationProcessor(OperationFactory operationFactory) {
    super(operationFactory.getChildClass());
    this.op = operationFactory.newInstance();
  }

  public Stream<InternalEvent> perform(Stream<InternalEvent> stream) {
    Stream<InternalEvent> output = null;

    if (this.op instanceof Operation) {
      output = stream.map(ievent -> {
        this.getRuntimeStat().start();
        try {
          InternalEvent i = ((Operation) op).perform(ievent);
          this.getSuccessCountStat().increment();
          return i;
        } catch (OperationException e) {
          this.getErrorCountStat().increment();
          return null;
        } finally {
          this.getRuntimeStat().stop();
        }
      });
    } else {
      output = stream.flatMap(ievent -> {
        this.getRuntimeStat().start();
        try {
          Stream<InternalEvent> s = ((MultiplexOperation) op).perform(ievent).stream();
          this.getSuccessCountStat().increment();
          return s;
        } catch (OperationException e) {
          this.getErrorCountStat().increment();
          return Stream.empty();
        } finally {
          this.getRuntimeStat().stop();
        }
      });
    }

    return output.filter(ievent -> {
      if (ievent != null) {
        return true;
      }
      return false;
    });
  }

  public BaseOperation getOperation() {
    return this.op;
  }

  public void setOperation(BaseOperation operation) {
    this.op = operation;
  }
}
