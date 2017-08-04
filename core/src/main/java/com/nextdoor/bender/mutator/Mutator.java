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

import java.util.List;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;

public interface Mutator {
  public DeserializedEvent mutateEvent(DeserializedEvent event) throws UnsupportedMutationException;
  public List<DeserializedEvent> mutateEvent(List<DeserializedEvent> event) throws UnsupportedMutationException;
  public InternalEvent mutateInternalEvent(InternalEvent event) throws UnsupportedMutationException;
  public List<InternalEvent> mutateInternalEvent(List<InternalEvent> events) throws UnsupportedMutationException;
}
