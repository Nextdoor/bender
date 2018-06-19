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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.ExtendedMessageFormat;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.FieldNotFoundException;
import com.nextdoor.bender.operation.EventOperation;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.operation.substitution.RegexSubSpecConfig.RegexSubField;

public class SubstitutionOperation implements EventOperation {
  private final List<SubSpecConfig<?>> subSpecs;

  public SubstitutionOperation(List<SubSpecConfig<?>> subSpecs) {
    this.subSpecs = subSpecs;
  }

  public Pair<String, Object> getFieldAndSource(DeserializedEvent devent,
      List<String> sourceFieldsNames, boolean asString) throws FieldNotFoundException {
    Object sourceValue = null;
    String foundSourceFieldName = null;
    for (String sourceFieldName : sourceFieldsNames) {
      try {
        if (asString) {
          sourceValue = devent.getFieldAsString(sourceFieldName);
        } else {
          sourceValue = devent.getField(sourceFieldName);
        }
        foundSourceFieldName = sourceFieldName;
        break;
      } catch (FieldNotFoundException e) {
        continue;
      }
    }

    if (sourceValue == null) {
      throw new FieldNotFoundException();
    }

    return new ImmutablePair<String, Object>(foundSourceFieldName, sourceValue);
  }


  /**
   * Matches a regex against a field and extracts matching groups.
   * 
   * @param devent
   * @param config
   * @return
   * @throws FieldNotFoundException
   */
  private Pair<String, Map<String, Object>> getRegexMatches(DeserializedEvent devent,
      RegexSubSpecConfig config) throws FieldNotFoundException {
    String foundSourceField = null;
    Pattern pattern = config.getRegex();
    Matcher matcher = null;

    for (String sourceField : config.getSrcFields()) {
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
      throw new FieldNotFoundException();
    }

    /*
     * Go through each match group in the field config and attempt to add that match group from the
     * regex match. If field type coercion does not succeed then field is skipped.
     */
    Map<String, Object> matchedGroups = new HashMap<String, Object>(matcher.groupCount());
    try {
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
            matchedGroups.put(field.getKey(), NumberUtils.createNumber(matchStrVal));
            break;
          case STRING:
            matchedGroups.put(field.getKey(), matchStrVal);
            break;
          default:
            matchedGroups.put(field.getKey(), matchStrVal);
            break;
        }
      }
    } catch (NumberFormatException e) {
      throw new FieldNotFoundException("matched field is not a number");
    }

    return new ImmutablePair<String, Map<String, Object>>(foundSourceField, matchedGroups);
  }

  private void doFieldSub(FieldSubSpecConfig config, DeserializedEvent devent,
      Map<String, Object> nested) {
    /*
     * Get the field value
     */
    Pair<String, Object> kv;
    try {
      kv = getFieldAndSource(devent, config.getSrcFields(), false);
    } catch (FieldNotFoundException e) {
      if (config.getFailSrcNotFound()) {
        throw new OperationException(e);
      }
      return;
    }

    /*
     * Set field value in nest or in deserialized event
     */
    if (nested != null) {
      nested.put(config.getKey(), kv.getValue());
      /*
       * Remove source field
       */
      if (config.getRemoveSrcField()) {
        devent.deleteField(kv.getKey());
      }
      return;
    }

    try {
      devent.setField(config.getKey(), kv.getValue());
    } catch (FieldNotFoundException e) {
      if (config.getFailDstNotFound()) {
        throw new OperationException(e);
      }
      return;
    }

    /*
     * Only remove if source field does not equal destination.
     */
    if (config.getRemoveSrcField() && !kv.getKey().equals(config.getKey())) {
      devent.deleteField(kv.getKey());
    }
  }

  private void doStaticSub(StaticSubSpecConfig config, DeserializedEvent devent,
      Map<String, Object> nested) {
    if (nested != null) {
      nested.put(config.getKey(), config.getValue());
      return;
    }

    try {
      devent.setField(config.getKey(), config.getValue());
    } catch (FieldNotFoundException e) {
      if (config.getFailDstNotFound()) {
        throw new OperationException(e);
      }
    }
  }

  /**
   * Performs a field substitution with metadata as the source.
   * 
   * @param ievent
   * @param config
   */
  private Map<String, Object> getMetadata(MetadataSubSpecConfig config, InternalEvent ievent) {
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

  private void doMetadataSub(MetadataSubSpecConfig config, InternalEvent ievent,
      DeserializedEvent devent, Map<String, Object> nested) {

    Map<String, Object> metadata = getMetadata(config, ievent);
    if (nested != null) {
      nested.put(config.getKey(), metadata);
      return;
    }

    try {
      devent.setField(config.getKey(), metadata);
    } catch (FieldNotFoundException e) {
      if (config.getFailDstNotFound()) {
        throw new OperationException(e);
      }
    }
  }

  /**
   * Performs a field substitution with the lambda invocation context as the source.
   * 
   * @param ievent
   * @param config
   */
  private Map<String, String> getContext(ContextSubSpecConfig config, InternalEvent ievent) {
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

  private void doContextSub(ContextSubSpecConfig config, InternalEvent ievent,
      DeserializedEvent devent, Map<String, Object> nested) {

    Map<String, String> context = getContext(config, ievent);
    if (nested != null) {
      nested.put(config.getKey(), context);
      return;
    }

    try {
      devent.setField(config.getKey(), context);
    } catch (FieldNotFoundException e) {
      if (config.getFailDstNotFound()) {
        throw new OperationException(e);
      }
    }
  }

  private void doStringSub(StringSubSpecConfig config, DeserializedEvent devent,
      Map<String, Object> nested) {
    Object[] values = new Object[config.getVariables().size()];
    List<String> keyToRemove = new ArrayList<String>();

    for (int i = 0; i < config.getVariables().size(); i++) {
      VariableConfig<?> variable = config.getVariables().get(i);

      /*
       * Get values
       */
      if (variable instanceof VariableConfig.FieldVariable) {
        Pair<String, Object> kv = null;
        try {
          kv = getFieldAndSource(devent, ((VariableConfig.FieldVariable) variable).getSrcFields(),
              true);

          if (((VariableConfig.FieldVariable) variable).getRemoveSrcField()) {
            keyToRemove.add(kv.getKey());
          }
        } catch (FieldNotFoundException e) {
          if (((VariableConfig.FieldVariable) variable).getFailSrcNotFound()) {
            throw new OperationException(e);
          }
        }

        if (kv != null) {
          values[i] = kv.getValue();
        } else {
          /*
           * This handles the case of when fail src not found is false
           */
          values[i] = null;
        }
      } else if (variable instanceof VariableConfig.StaticVariable) {
        values[i] = ((VariableConfig.StaticVariable) variable).getValue();
      }
    }

    /*
     * Format string with values
     */
    ExtendedMessageFormat mf = config.getMessageFormat();
    String formatted = mf.format(values);

    /*
     * Perform substitution
     */
    if (nested != null) {
      nested.put(config.getKey(), formatted);
      keyToRemove.forEach(fieldName -> {
        try {
          devent.removeField(fieldName);
        } catch (FieldNotFoundException e) {
        }
      });
      return;
    }

    try {
      devent.setField(config.getKey(), formatted);
    } catch (FieldNotFoundException e) {
      if (config.getFailDstNotFound()) {
        throw new OperationException(e);
      }
    }

    /*
     * Remove source fields
     */
    keyToRemove.forEach(fieldName -> {
      try {
        devent.removeField(fieldName);
      } catch (FieldNotFoundException e) {
      }
    });
  }

  private void doRegexSub(RegexSubSpecConfig config, DeserializedEvent devent,
      Map<String, Object> nested) {
    Pair<String, Map<String, Object>> kv;
    try {
      kv = getRegexMatches(devent, config);
    } catch (FieldNotFoundException e) {
      if (config.getFailSrcNotFound()) {
        throw new OperationException(e);
      }
      return;
    }

    /*
     * Short circuit if this is adding to a nested operation
     */
    if (nested != null) {
      nested.putAll(kv.getValue());

      /*
       * Remove source field
       */
      if (config.getRemoveSrcField()) {
        try {
          devent.removeField(kv.getKey());
        } catch (FieldNotFoundException e) {
          if (config.getFailSrcNotFound()) {
            throw new OperationException(e);
          }
        }
      }
      return;
    }

    ((Map<String, Object>) kv.getValue()).forEach((k, v) -> {
      try {
        devent.setField(k, v);
      } catch (FieldNotFoundException e) {
        if (config.getFailDstNotFound()) {
          throw new OperationException(e);
        }
      }
    });

    /*
     * Do not remove source field if it has been replaced by a regex group.
     */
    if (config.getRemoveSrcField() && !kv.getValue().containsKey(kv.getKey())) {
      try {
        devent.removeField(kv.getKey());
      } catch (FieldNotFoundException e) {
        if (config.getFailSrcNotFound()) {
          throw new OperationException(e);
        }
      }
    }
  }

  private void doNestedSub(NestedSubSpecConfig config, InternalEvent ievent,
      DeserializedEvent devent, Map<String, Object> nested) {
    for (SubSpecConfig<?> subSpec : config.getSubstitutions()) {
      if (subSpec instanceof FieldSubSpecConfig) {
        doFieldSub((FieldSubSpecConfig) subSpec, devent, nested);
      } else if (subSpec instanceof StaticSubSpecConfig) {
        doStaticSub((StaticSubSpecConfig) subSpec, devent, nested);
      } else if (subSpec instanceof MetadataSubSpecConfig) {
        doMetadataSub((MetadataSubSpecConfig) subSpec, ievent, devent, nested);
      } else if (subSpec instanceof ContextSubSpecConfig) {
        doContextSub((ContextSubSpecConfig) subSpec, ievent, devent, nested);
      } else if (subSpec instanceof RegexSubSpecConfig) {
        doRegexSub((RegexSubSpecConfig) subSpec, devent, nested);
      } else if (subSpec instanceof StringSubSpecConfig) {
        doStringSub((StringSubSpecConfig) subSpec, devent, nested);
      } else if (subSpec instanceof NestedSubSpecConfig) {
        Map<String, Object> innerNest = new HashMap<String, Object>();
        doNestedSub((NestedSubSpecConfig) subSpec, ievent, devent, innerNest);

        if (nested != null) {
          nested.put(subSpec.getKey(), innerNest);
          return;
        }

        try {
          devent.setField(subSpec.getKey(), innerNest);
        } catch (FieldNotFoundException e) {
          if (subSpec.getFailDstNotFound()) {
            throw new OperationException(e);
          }
        }
      }
    }
  }

  @Override
  public InternalEvent perform(InternalEvent ievent) {
    DeserializedEvent devent = ievent.getEventObj();
    if (devent == null || devent.getPayload() == null) {
      return ievent;
    }

    for (SubSpecConfig<?> subSpec : subSpecs) {
      if (subSpec instanceof FieldSubSpecConfig) {
        doFieldSub((FieldSubSpecConfig) subSpec, devent, null);
      } else if (subSpec instanceof StaticSubSpecConfig) {
        doStaticSub((StaticSubSpecConfig) subSpec, devent, null);
      } else if (subSpec instanceof MetadataSubSpecConfig) {
        doMetadataSub((MetadataSubSpecConfig) subSpec, ievent, devent, null);
      } else if (subSpec instanceof ContextSubSpecConfig) {
        doContextSub((ContextSubSpecConfig) subSpec, ievent, devent, null);
      } else if (subSpec instanceof RegexSubSpecConfig) {
        doRegexSub((RegexSubSpecConfig) subSpec, devent, null);
      } else if (subSpec instanceof StringSubSpecConfig) {
        doStringSub((StringSubSpecConfig) subSpec, devent, null);
      } else if (subSpec instanceof NestedSubSpecConfig) {
        Map<String, Object> innerNest = new HashMap<String, Object>();
        doNestedSub((NestedSubSpecConfig) subSpec, ievent, devent, innerNest);

        try {
          devent.setField(subSpec.getKey(), innerNest);
        } catch (FieldNotFoundException e) {
          if (subSpec.getFailDstNotFound()) {
            throw new OperationException(e);
          }
        }
      }
    }

    return ievent;
  }

  public List<SubSpecConfig<?>> getSubSpecs() {
    return this.subSpecs;
  }
}
