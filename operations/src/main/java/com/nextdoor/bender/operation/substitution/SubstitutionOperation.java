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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.math.NumberUtils;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.FieldNotFoundException;
import com.nextdoor.bender.operation.Operation;
import com.nextdoor.bender.operation.substitution.RegexSubSpecConfig.RegexSubField;

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
  private Object getField(DeserializedEvent devent, FieldSubSpecConfig config) {
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
      } catch (FieldNotFoundException e) {
        continue;
      }

      if (sourceValue != null) {
        break;
      }
    }

    return sourceValue;
  }

  /**
   * Performs a field substitution with metadata as the source.
   * 
   * @param ievent
   * @param config
   */
  private Map<String, Object> getMetadata(InternalEvent ievent, MetadataSubSpecConfig config) {
    List<String> includes = config.getIncludes();
    List<String> excludes = config.getExcludes();
    Map<String, Object> metadata = new HashMap<String, Object>(ievent.getEventMetadata());

    if (!includes.isEmpty()) {
      metadata.keySet().retainAll(includes);
    }

    excludes.forEach(exclude -> {
      metadata.remove(exclude);
    });

    return metadata;
  }

  /**
   * Performs a field substitution with the lambda invocation context as the source.
   * 
   * @param ievent
   * @param config
   */
  private Map<String, String> getContext(InternalEvent ievent, ContextSubSpecConfig config) {
    List<String> includes = config.getIncludes();
    List<String> excludes = config.getExcludes();
    Map<String, String> contexts = ievent.getCtx().getContextAsMap();

    if (!includes.isEmpty()) {
      contexts.keySet().retainAll(includes);
    }

    excludes.forEach(exclude -> {
      contexts.remove(exclude);
    });

    return contexts;
  }

  /**
   * Matches a regex against a field and extracts matching groups.
   * 
   * @param ievent
   * @param devent
   * @param config
   * @return
   */
  private Map<String, Object> getRegexMatches(InternalEvent ievent, DeserializedEvent devent,
      RegexSubSpecConfig config) {
    String foundSourceField = null;
    Pattern pattern = config.getRegex();
    Matcher matcher = null;

    for (String sourceField : config.getSourceFields()) {
      String sourceValue;
      try {
        sourceValue = devent.getFieldAsString(sourceField);
      } catch (FieldNotFoundException e) {
        continue;
      }

      matcher = pattern.matcher(sourceValue);

      if (matcher.find()) {
        /*
         * Keep track of the field name that we use so it can be removed later.
         */
        foundSourceField = sourceField;
        break;
      }
    }

    if (foundSourceField == null) {
      return Collections.emptyMap();
    }

    /*
     * Go through each match group in the field config and attempt to add that match group from the
     * regex match. If field type coercion does not succeed then field is skipped.
     */
    Map<String, Object> matchedGroups = new HashMap<String, Object>(matcher.groupCount());
    for (RegexSubField field : config.getFields()) {
      String matchStrVal = matcher.group(field.getRegexGroupName());

      if (matchStrVal == null) {
        continue;
      }

      switch (field.getType()) {
        case BOOLEAN:
          matchedGroups.put(field.getKey(), Boolean.parseBoolean(matchStrVal));
          break;
        case NUMBER:
          try {
            matchedGroups.put(field.getKey(), NumberUtils.createNumber(matchStrVal));
          } catch (NumberFormatException e) {
            continue;
          }
          break;
        case STRING:
          matchedGroups.put(field.getKey(), matchStrVal);
          break;
        default:
          matchedGroups.put(field.getKey(), matchStrVal);
          break;
      }
    }

    /*
     * Remove source field
     */
    if (config.getRemoveSourceField()) {
      try {
        devent.removeField(foundSourceField);
      } catch (FieldNotFoundException e) {
      }
    }

    return matchedGroups;
  }

  /**
   * Creates a Map object from other substitutions.
   * 
   * @param ievent
   * @param devent
   * @param subSpecs
   * @return Map containing substitutions
   */
  private Map<String, Object> getNested(InternalEvent ievent, DeserializedEvent devent,
      List<SubSpecConfig<?>> subSpecs) {
    Map<String, Object> map = new HashMap<String, Object>(subSpecs.size());

    for (SubSpecConfig<?> subSpec : subSpecs) {
      if (subSpec instanceof RegexSubSpecConfig) {
        map.putAll((Map<String, Object>) getValue(ievent, devent, subSpec));
      } else {
        map.put(subSpec.getKey(), getValue(ievent, devent, subSpec));
      }
    }

    return map;
  }

  private Object getValue(InternalEvent ievent, DeserializedEvent devent,
      SubSpecConfig<?> subSpec) {
    Object value = null;
    if (subSpec instanceof FieldSubSpecConfig) {
      value = getField(devent, (FieldSubSpecConfig) subSpec);
    } else if (subSpec instanceof StaticSubSpecConfig) {
      value = ((StaticSubSpecConfig) subSpec).getValue();
    } else if (subSpec instanceof MetadataSubSpecConfig) {
      value = getMetadata(ievent, (MetadataSubSpecConfig) subSpec);
    } else if (subSpec instanceof ContextSubSpecConfig) {
      value = getContext(ievent, (ContextSubSpecConfig) subSpec);
    } else if (subSpec instanceof NestedSubSpecConfig) {
      value = getNested(ievent, devent, ((NestedSubSpecConfig) subSpec).getSubstitutions());
    } else if (subSpec instanceof RegexSubSpecConfig) {
      value = getRegexMatches(ievent, devent, (RegexSubSpecConfig) subSpec);
    }

    return value;
  }

  @Override
  public InternalEvent perform(InternalEvent ievent) {
    DeserializedEvent devent = ievent.getEventObj();
    if (devent == null || devent.getPayload() == null) {
      return ievent;
    }

    for (SubSpecConfig<?> subSpec : subSpecs) {
      Object value = getValue(ievent, devent, subSpec);

      if (subSpec instanceof RegexSubSpecConfig) {
        ((Map<String, Object>) value).forEach((k, v) -> {
          try {
            devent.setField(k, v);
          } catch (FieldNotFoundException e) {
          }
        });
      } else {
        try {
          devent.setField(subSpec.getKey(), value);
        } catch (FieldNotFoundException e) {
          continue;
        }
      }
    }

    return ievent;
  }

  public List<SubSpecConfig<?>> getSubSpecs() {
    return this.subSpecs;
  }
}
