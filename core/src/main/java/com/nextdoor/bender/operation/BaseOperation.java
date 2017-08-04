package com.nextdoor.bender.operation;

import com.nextdoor.bender.InternalEvent;

public interface BaseOperation {
  Object perform(InternalEvent ievent);
}
