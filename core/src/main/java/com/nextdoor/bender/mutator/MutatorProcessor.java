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
 * Copyright 2016 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.mutator;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.Sets;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.monitoring.MonitoredProcess;

/**
 * Wraps a {@link Mutator} with a {@link MonitoredProcess} in order to keep stats on mutations
 * during a function run.
 */
public class MutatorProcessor extends MonitoredProcess {
  private BaseMutator mutator;

  public MutatorProcessor(MutatorFactory mutatorFactory) {
    super(mutatorFactory.getChildClass());
    this.mutator = mutatorFactory.newInstance();
  }

  protected InternalEvent mutate(InternalEvent ievent) {
    if (ievent.getEventObj() == null) {
      return ievent;
    }

    try {
      return ((Mutator) this.mutator).mutateEvent(ievent);
    } catch (UnsupportedMutationException e) {
      throw new RuntimeException(e);
    }
  }
  
  protected List<InternalEvent> multiplexMutate(InternalEvent ievent) {
    if (ievent.getEventObj() == null) {
      return Collections.emptyList();
    }

    try {
      return ((MultiplexMutator) this.mutator).mutateEvent(ievent);
    } catch (UnsupportedMutationException e) {
      throw new RuntimeException(e);
    }
  }

  public Stream<InternalEvent> mutate(Stream<InternalEvent> stream) {
    this.getRuntimeStat().start();

    Stream<InternalEvent> output = null;
    try {
      if (this.mutator instanceof Mutator) {
        output = stream.map(ievent -> { return mutate(ievent);});
      } else if (this.mutator instanceof MultiplexMutator) {
        output = stream.flatMap(ievent -> { return multiplexMutate(ievent).stream();});
      }
      this.getSuccessCountStat().increment();
      return output;
    } catch (RuntimeException e) {
      this.getErrorCountStat().increment();
      throw e;
    } finally {
      this.getRuntimeStat().stop();
      this.getSuccessCountStat().increment();
    }
  }

  public BaseMutator getMutator() {
    return this.mutator;
  }

  public void setMutator(Mutator mutator) {
    this.mutator = mutator;
  }
}
