package com.nextdoor.bender.operation;

public class OperationException extends RuntimeException {
  private static final long serialVersionUID = 468669144414527994L;

  public OperationException(String message) {
    super(message);
  }

  public OperationException(Throwable t) {
    super(t);
  }
}
