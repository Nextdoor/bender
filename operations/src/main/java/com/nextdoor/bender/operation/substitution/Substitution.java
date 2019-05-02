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

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.FieldNotFoundException;

public abstract class Substitution {
  protected abstract void doSubstitution(InternalEvent ievent, DeserializedEvent devent,
      Map<String, Object> nested);

  protected abstract void doSubstitution(InternalEvent ievent, DeserializedEvent devent);

  protected static Pair<String, Object> getFieldAndSource(DeserializedEvent devent,
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
      System.out.println(devent.getPayload());
      throw new FieldNotFoundException("unable to find field in: " + sourceFieldsNames);
    }

    return new ImmutablePair<String, Object>(foundSourceFieldName, sourceValue);
  }
}
