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
import java.util.NoSuchElementException;

import com.amazonaws.services.lambda.runtime.Client;
import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.operation.Operation;

public class SubstitutionOperation implements Operation {
  private final List<SubSpecConfig<?>> subSpecs;

  public SubstitutionOperation(List<SubSpecConfig<?>> subSpecs) {
    this.subSpecs = subSpecs;
  }

  @Override
  public InternalEvent perform(InternalEvent ievent) {

    DeserializedEvent devent = ievent.getEventObj();

    if (devent == null || devent.getPayload() == null) {
      return ievent;
    }

    for (SubSpecConfig<?> subSpec : subSpecs) {
      if (subSpec instanceof FieldSubSpecConfig) {
        try {
          devent.setField(subSpec.getKey(),
              devent.getField(((FieldSubSpecConfig) subSpec).getSourceField()));
        } catch (NoSuchElementException e) {
        }
      } else if (subSpec instanceof StaticSubSpecConfig) {
        devent.setField(subSpec.getKey(), ((StaticSubSpecConfig) subSpec).getValue());
      } else if (subSpec instanceof MetadataSubSpecConfig) {
        MetadataSubSpecConfig m = (MetadataSubSpecConfig) subSpec;

        List<String> includes = m.getIncludes();
        List<String> excludes = m.getExcludes();
        Map<String, Object> metadata = new HashMap<String, Object>(ievent.getEventMetadata());

        if (!includes.isEmpty()) {
          metadata.keySet().retainAll(includes);
        }

        excludes.forEach(exclude -> {
          metadata.remove(exclude);
        });

        devent.setField(subSpec.getKey(), metadata);
      } else if (subSpec instanceof ContextSubSpecConfig) {
        ContextSubSpecConfig c = (ContextSubSpecConfig) subSpec;

        List<String> includes = c.getIncludes();
        List<String> excludes = c.getExcludes();
        Map<String, String> contexts = ievent.getCtx().getContextAsMap();

        if (!includes.isEmpty()) {
          contexts.keySet().retainAll(includes);
        }

        excludes.forEach(exclude -> {
          contexts.remove(exclude);
        });

        devent.setField(subSpec.getKey(), contexts);
      }
    }

    return ievent;
  }

  public List<SubSpecConfig<?>> getSubSpecs() {
    return this.subSpecs;
  }
}
