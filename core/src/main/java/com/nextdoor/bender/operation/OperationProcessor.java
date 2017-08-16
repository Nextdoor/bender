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

import org.apache.log4j.Logger;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.monitoring.MonitoredProcess;

public class OperationProcessor extends MonitoredProcess {
  private static final Logger logger = Logger.getLogger(OperationProcessor.class);
  private BaseOperation op;

  public OperationProcessor(OperationFactory operationFactory) {
    super(operationFactory.getChildClass());
    this.op = operationFactory.newInstance();
  }

  /**
   * This method sets up an operation to be performed on a stream. It is important to note that
   * counting, time keeping, and exception handling must be done within the map/flatmap action as
   * this method itself does not evaluate records and is only called once per function invocation to
   * setup the stream pipeline.
   * 
   * @param stream
   * @return new stream with operation map/flatmap added
   */
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
    } else if (this.op instanceof MultiplexOperation) {
      /*
       * MultiplexOperations require the use of flatmap which allows a single stream item to produce
       * multiple results.
       */
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
    } else {
      throw new OperationException("Invalid type of operation");
    }

    /*
     * Filter out events if an operation did something that resulted in a null event or payload.
     * This protects future operations from running on invalid data.
     */
    return output.filter(ievent -> {
      if (ievent == null) {
        logger.warn(op.getClass().getName() + " produced a null InternalEvent");
        return false;
      }
      if (ievent.getEventObj() == null) {
        logger.warn(op.getClass().getName() + " produced a null DeserializedEvent");
        return false;
      }
      if (ievent.getEventObj().getPayload() == null) {
        logger.warn(op.getClass().getName() + " produced a null DeserializedEvent payload");
        return false;
      }

      return true;
    });
  }

  public BaseOperation getOperation() {
    return this.op;
  }

  public void setOperation(BaseOperation operation) {
    this.op = operation;
  }
}
