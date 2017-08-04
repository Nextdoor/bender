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
