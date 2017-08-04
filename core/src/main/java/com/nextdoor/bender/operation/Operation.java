package com.nextdoor.bender.operation;

import com.nextdoor.bender.InternalEvent;

public interface Operation extends BaseOperation {
  InternalEvent perform(InternalEvent ievent);
}
