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

package com.nextdoor.bender.operation.substitution.formatted;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.ExtendedMessageFormat;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.FieldNotFoundException;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.operation.substitution.Substitution;
import com.nextdoor.bender.operation.substitution.Variable;

public class FormattedSubstitution extends Substitution {
  private final String key;
  private final ExtendedMessageFormat format;
  private final List<Variable<?>> variables;
  private final boolean failDstNotFound;
  
  public FormattedSubstitution(String key, ExtendedMessageFormat format,
      List<Variable<?>> variables, boolean failDstNotFound) {
    this.key = key;
    this.format = format;
    this.variables = variables;
    this.failDstNotFound = failDstNotFound;
  }

  @Override
  protected void doSubstitution(InternalEvent ievent, DeserializedEvent devent,
      Map<String, Object> nested) {
    Object[] values = new Object[this.variables.size()];
    List<String> keyToRemove = new ArrayList<>();

    for (int i = 0; i < this.variables.size(); i++) {
      Variable<?> variable = this.variables.get(i);

      /*
       * Get values
       */
      if (variable instanceof Variable.FieldVariable) {
        Pair<String, Object> kv = null;
        try {
          kv = getFieldAndSource(devent, ((Variable.FieldVariable) variable).getSrcFields(),
              true);

          if (((Variable.FieldVariable) variable).getRemoveSrcField()) {
            keyToRemove.add(kv.getKey());
          }
        } catch (FieldNotFoundException e) {
          if (((Variable.FieldVariable) variable).getFailSrcNotFound()) {
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
      } else if (variable instanceof Variable.StaticVariable) {
        values[i] = ((Variable.StaticVariable) variable).getValue();
      }
    }

    /*
     * Format string with values
     */
    String formatted = format.format(values);

    /*
     * Perform substitution
     */
    if (nested != null) {
      nested.put(this.key, formatted);
      keyToRemove.forEach(fieldName -> {
        if (fieldName.equals(this.key)) {
          return;
        }
        try {
          devent.removeField(fieldName);
        } catch (FieldNotFoundException e) {
        }
      });
      return;
    }

    try {
      devent.setField(this.key, formatted);
    } catch (FieldNotFoundException e) {
      if (this.failDstNotFound) {
        throw new OperationException(e);
      }
    }

    /*
     * Remove source fields
     */
    keyToRemove.forEach(fieldName -> {
      if (fieldName.equals(this.key)) {
        return;
      }
      try {
        devent.removeField(fieldName);
      } catch (FieldNotFoundException e) {
      }
    });
  }

  @Override
  protected void doSubstitution(InternalEvent ievent, DeserializedEvent devent) {
    doSubstitution(ievent, devent, null);
  }
}
