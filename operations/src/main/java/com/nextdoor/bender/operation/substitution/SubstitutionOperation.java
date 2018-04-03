/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * Copyright 2018 Nextdoor.com, Inc
 */

package com.nextdoor.bender.operation.substitution;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.operation.Operation;

public class SubstitutionOperation implements Operation {
  private final ArrayList<SubstitutionSpec> subSpecs;

  public SubstitutionOperation(ArrayList<SubstitutionSpec> subSpecs) {
    this.subSpecs = subSpecs;
  }

  @Override
  public InternalEvent perform(InternalEvent ievent) {

    DeserializedEvent devent = ievent.getEventObj();

    if (devent == null || devent.getPayload() == null) {
      return ievent;
    }

    for (SubstitutionSpec subSpec : subSpecs) {
      switch (subSpec.getInterpreter()) {
        case STATIC:
          devent.setField(subSpec.getKey(), subSpec.getValue());
          break;
        case FIELD:
          try {
            devent.setField(subSpec.getKey(), devent.getField(subSpec.getValue()));
          } catch (NoSuchElementException e) {
            break;
          }
          break;
      }
    }

    return ievent;
  }

  public ArrayList<SubstitutionSpec> getSubSpecs() {
    return this.subSpecs;
  }
}
