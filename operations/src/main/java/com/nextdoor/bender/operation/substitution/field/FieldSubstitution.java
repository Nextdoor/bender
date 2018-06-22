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

package com.nextdoor.bender.operation.substitution.field;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.FieldNotFoundException;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.operation.substitution.Substitution;

public class FieldSubstitution extends Substitution {
  
  private final String key;
  private final List<String> srcFields;
  private final boolean removeSrcField;
  private final boolean failSrcNotFound;
  private final boolean failDstNotFound;

  public FieldSubstitution(String key, List<String> srcFields, boolean removeSrcField,
      boolean failSrcNotFound, boolean failDstNotFound) {
    this.key = key;
    this.srcFields = srcFields;
    this.removeSrcField = removeSrcField;
    this.failSrcNotFound = failSrcNotFound;
    this.failDstNotFound = failDstNotFound;
  }

  @Override
  protected void doSubstitution(InternalEvent ievent, DeserializedEvent devent,
      Map<String, Object> nested) {
    Pair<String, Object> kv;
    try {
      kv = getFieldAndSource(devent, srcFields, false);
    } catch (FieldNotFoundException e) {
      if (this.failSrcNotFound) {
        throw new OperationException(e);
      }
      return;
    }

    nested.put(this.key, kv.getValue());
    /*
     * Remove source field
     */
    if (this.removeSrcField) {
      devent.deleteField(kv.getKey());
    }
  }

  @Override
  protected void doSubstitution(InternalEvent ievent, DeserializedEvent devent) {
    /*
     * Get the field value
     */
    Pair<String, Object> kv;
    try {
      kv = getFieldAndSource(devent, srcFields, false);
    } catch (FieldNotFoundException e) {
      if (this.failSrcNotFound) {
        throw new OperationException(e);
      }
      return;
    }

    try {
      devent.setField(this.key, kv.getValue());
    } catch (FieldNotFoundException e) {
      if (this.failDstNotFound) {
        throw new OperationException(e);
      }
      return;
    }

    /*
     * Only remove if source field does not equal destination.
     */
    if (this.removeSrcField && !kv.getKey().equals(this.key)) {
      devent.deleteField(kv.getKey());
    }
  }
}
