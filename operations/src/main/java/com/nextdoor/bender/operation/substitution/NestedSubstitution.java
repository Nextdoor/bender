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
 * Copyright 2018 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.operation.substitution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.FieldNotFoundException;
import com.nextdoor.bender.operation.OperationException;

public class NestedSubstitution extends Substitution {
  private final String key;
  private final List<Substitution> substitutions;
  private final boolean failDstNotFound;

  public NestedSubstitution(String key, List<Substitution> substitutions, boolean failDstNotFound) {
    this.key = key;
    this.substitutions = substitutions;
    this.failDstNotFound = failDstNotFound;
  }

  @Override
  protected void doSubstitution(InternalEvent ievent, DeserializedEvent devent,
      Map<String, Object> nested) {

    for (Substitution sub : this.substitutions) {
      if (sub instanceof NestedSubstitution) {
        /*
         * If the nested substitution is itself a nested substitution then
         * we have to construct a new map to hold the nested substitutions
         * and then add that map to the map passed to this function.
         */
        NestedSubstitution nsub = (NestedSubstitution) sub;
        Map<String, Object> innerNest = new HashMap<>(nsub.substitutions.size());
        sub.doSubstitution(ievent, devent, innerNest);
        nested.put(nsub.key, innerNest);
      } else {
        sub.doSubstitution(ievent, devent, nested);
      }
    }
  }

  @Override
  protected void doSubstitution(InternalEvent ievent, DeserializedEvent devent) {
    Map<String, Object> innerNest = new HashMap<>(this.substitutions.size());
    doSubstitution(ievent, devent, innerNest);

    try {
      devent.setField(this.key, innerNest);
    } catch (FieldNotFoundException e) {
      if (this.failDstNotFound) {
        throw new OperationException(e);
      }
    }
  }
}
