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

package com.nextdoor.bender.operation.substitution.regex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.FieldNotFoundException;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.operation.substitution.Substitution;
import com.nextdoor.bender.operation.substitution.regex.RegexSubstitutionConfig.RegexSubField;

public class RegexSubstitution extends Substitution {
  private final List<String> srcFields;
  private final Pattern pattern;
  private final List<RegexSubField> fields;
  private final boolean removeSrcField;
  private final boolean failSrcNotFound;
  private final boolean failDstNotFound;

  public RegexSubstitution(List<String> srcFields, Pattern pattern, List<RegexSubField> fields,
      boolean removeSrcField, boolean failSrcNotFound, boolean failDstNotFound) {
    this.srcFields = srcFields;
    this.pattern = pattern;
    this.fields = fields;
    this.removeSrcField = removeSrcField;
    this.failSrcNotFound = failSrcNotFound;
    this.failDstNotFound = failDstNotFound;
  }

  @Override
  protected void doSubstitution(InternalEvent ievent, DeserializedEvent devent,
      Map<String, Object> nested) {
    Pair<String, Map<String, Object>> kv;
    try {
      kv = getRegexMatches(devent);
    } catch (FieldNotFoundException e) {
      if (this.failSrcNotFound) {
        throw new OperationException(e);
      }
      return;
    }

    nested.putAll(kv.getValue());

    /*
     * Remove source field
     */
    if (this.removeSrcField) {
      try {
        devent.removeField(kv.getKey());
      } catch (FieldNotFoundException e) {
        if (this.failSrcNotFound) {
          throw new OperationException(e);
        }
      }
    }
  }

  @Override
  protected void doSubstitution(InternalEvent ievent, DeserializedEvent devent) {
    Pair<String, Map<String, Object>> kv;
    try {
      kv = getRegexMatches(devent);
    } catch (FieldNotFoundException e) {
      if (this.failSrcNotFound) {
        throw new OperationException(e);
      }
      return;
    }


    ((Map<String, Object>) kv.getValue()).forEach((k, v) -> {
      try {
        devent.setField(k, v);
      } catch (FieldNotFoundException e) {
        if (this.failDstNotFound) {
          throw new OperationException(e);
        }
      }
    });

    /*
     * Do not remove source field if it has been replaced by a regex group.
     */
    if (this.removeSrcField && !kv.getValue().containsKey(kv.getKey())) {
      try {
        devent.removeField(kv.getKey());
      } catch (FieldNotFoundException e) {
        if (this.failSrcNotFound) {
          throw new OperationException(e);
        }
      }
    }
  }


  /**
   * Matches a regex against a field and extracts matching groups.
   * 
   * @param devent
   * @param config
   * @return
   * @throws FieldNotFoundException
   */
  private Pair<String, Map<String, Object>> getRegexMatches(DeserializedEvent devent)
      throws FieldNotFoundException {
    String foundSourceField = null;
    Matcher matcher = null;

    for (String sourceField : this.srcFields) {
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
      for (RegexSubField field : this.fields) {
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
}
