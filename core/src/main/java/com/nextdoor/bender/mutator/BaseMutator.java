/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright $year Nextdoor.com, Inc
 */

package com.nextdoor.bender.mutator;

import java.util.List;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;

public abstract class BaseMutator implements Mutator {

  @Override
  public List<DeserializedEvent> mutateEvent(List<DeserializedEvent> events) throws UnsupportedMutationException {
    for (DeserializedEvent e : events) {
      this.mutateEvent(e);
    }
    return events;
  }

  @Override
  public InternalEvent mutateInternalEvent(InternalEvent event) throws UnsupportedMutationException {
    this.mutateEvent(event.getEventObj());
    return event;
  }

  @Override
  public List<InternalEvent> mutateInternalEvent(List<InternalEvent> events) throws UnsupportedMutationException {
    for (InternalEvent e : events) {
      this.mutateInternalEvent(e);
    }
    return events;
  }
}
