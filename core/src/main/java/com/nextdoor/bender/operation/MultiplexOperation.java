package com.nextdoor.bender.operation;

import java.util.List;

import com.nextdoor.bender.InternalEvent;

public interface MultiplexOperation extends BaseOperation {
  List<InternalEvent> perform(InternalEvent ievent);
}
