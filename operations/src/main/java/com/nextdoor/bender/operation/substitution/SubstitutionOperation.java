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

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.operation.Operation;

public class SubstitutionOperation implements Operation {
  private final List<SubSpecConfig<?>> subSpecs;

  public SubstitutionOperation(List<SubSpecConfig<?>> subSpecs) {
    this.subSpecs = subSpecs;
  }

  /**
   * Perform a field substitution from one field to another. By default this is a copy operation but
   * turns into move if RemoveSourceField is specified.
   * 
   * @param devent
   * @param config
   */
  private void performFieldSub(DeserializedEvent devent, FieldSubSpecConfig config) {
    Object sourceValue = null;

    /*
     * Pick first non null source field value
     */
    for (String sourceField : config.getSourceFields()) {
      try {
        if (!config.getRemoveSourceField()) {
          sourceValue = devent.getField(sourceField);
        } else {
          sourceValue = devent.removeField(sourceField);
        }

        if (sourceValue != null) {
          break;
        }
      } catch (NoSuchElementException e) {
      }
    }

    devent.setField(config.getKey(), sourceValue);
  }

  /**
   * Performs a field substitution with metadata as the source.
   * 
   * @param devent
   * @param ievent
   * @param config
   */
  private void performMetadataSub(DeserializedEvent devent, InternalEvent ievent,
      MetadataSubSpecConfig config) {
    List<String> includes = config.getIncludes();
    List<String> excludes = config.getExcludes();
    Map<String, Object> metadata = new HashMap<String, Object>(ievent.getEventMetadata());

    if (!includes.isEmpty()) {
      metadata.keySet().retainAll(includes);
    }

    excludes.forEach(exclude -> {
      metadata.remove(exclude);
    });

    devent.setField(config.getKey(), metadata);
  }

  /**
   * Performs a field substitution with the lambda invocation context as the source.
   * 
   * @param devent
   * @param ievent
   * @param config
   */
  private void performContextSub(DeserializedEvent devent, InternalEvent ievent,
      ContextSubSpecConfig config) {
    List<String> includes = config.getIncludes();
    List<String> excludes = config.getExcludes();
    Map<String, String> contexts = ievent.getCtx().getContextAsMap();

    if (!includes.isEmpty()) {
      contexts.keySet().retainAll(includes);
    }

    excludes.forEach(exclude -> {
      contexts.remove(exclude);
    });

    devent.setField(config.getKey(), contexts);
  }

  @Override
  public InternalEvent perform(InternalEvent ievent) {
    DeserializedEvent devent = ievent.getEventObj();
    if (devent == null || devent.getPayload() == null) {
      return ievent;
    }

    for (SubSpecConfig<?> subSpec : subSpecs) {
      if (subSpec instanceof FieldSubSpecConfig) {
        performFieldSub(devent, (FieldSubSpecConfig) subSpec);
      } else if (subSpec instanceof StaticSubSpecConfig) {
        devent.setField(subSpec.getKey(), ((StaticSubSpecConfig) subSpec).getValue());
      } else if (subSpec instanceof MetadataSubSpecConfig) {
        performMetadataSub(devent, ievent, (MetadataSubSpecConfig) subSpec);
      } else if (subSpec instanceof ContextSubSpecConfig) {
        performContextSub(devent, ievent, (ContextSubSpecConfig) subSpec);
      }
    }

    return ievent;
  }

  public List<SubSpecConfig<?>> getSubSpecs() {
    return this.subSpecs;
  }
}
