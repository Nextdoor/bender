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

import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.monitoring.MonitoredProcess;

/**
 * Wraps a {@link Mutator} with a {@link MonitoredProcess} in order to keep stats on mutations
 * during a function run.
 */
public class MutatorProcessor extends MonitoredProcess {
  private Mutator mutator;

  public MutatorProcessor(MutatorFactory mutatorFactory) {
    super(mutatorFactory.getChildClass());
    this.mutator = mutatorFactory.newInstance();
  }

  public void mutate(DeserializedEvent event) throws UnsupportedMutationException {
    this.getRuntimeStat().start();

    try {
      this.mutator.mutateEvent(event);
    } catch (UnsupportedMutationException e) {
      this.getErrorCountStat().increment();
      throw e;
    } finally {
      this.getRuntimeStat().stop();
    }

    this.getSuccessCountStat().increment();
  }

  public Mutator getMutator() {
    return this.mutator;
  }

  public void setMutator(Mutator mutator) {
    this.mutator = mutator;
  }
}
