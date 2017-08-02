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
 * Copyright $year Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.testutils;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.mutator.BaseMutator;
import com.nextdoor.bender.mutator.Mutator;
import com.nextdoor.bender.mutator.MutatorConfig;
import com.nextdoor.bender.mutator.MutatorFactory;
import com.nextdoor.bender.mutator.UnsupportedMutationException;

public class DummyMutatorHelper {
  public static class DummyMutator extends BaseMutator {
    @Override
    public DeserializedEvent mutateEvent(DeserializedEvent event) throws UnsupportedMutationException {
      return event;
    }
  }

  @JsonTypeName("DummyMutatorHelper$DummyMutatorConfig")
  public static class DummyMutatorConfig extends MutatorConfig {

    @Override
    public Class<DummyMutatorFactory> getFactoryClass() {
      return DummyMutatorFactory.class;
    }
  }

  public static class DummyMutatorFactory implements MutatorFactory {
    public Mutator mutator;

    public DummyMutatorFactory() {}

    public DummyMutatorFactory(Mutator mutator) {
      this.mutator = mutator;
    }

    @Override
    public Mutator newInstance() {
      return this.mutator == null ? new DummyMutator() : this.mutator;
    }

    @Override
    public Class<DummyMutator> getChildClass() {
      return DummyMutator.class;
    }

    @Override
    public void setConf(AbstractConfig config) {}
  }
}
