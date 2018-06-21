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

package com.nextdoor.bender.operation.substitution.ztatic;

import java.util.Map;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.FieldNotFoundException;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.operation.substitution.Substitution;

public class StaticSubstitution extends Substitution {
  private final String key;
  private final String value;
  private final boolean failDstNotFound;

  public StaticSubstitution(String key, String value, boolean failDstNotFound) {
    this.key = key;
    this.value = value;
    this.failDstNotFound = failDstNotFound;
  }

  @Override
  protected void doSubstitution(InternalEvent ievent, DeserializedEvent devent,
      Map<String, Object> nested) {
    nested.put(this.key, this.value);
  }

  @Override
  protected void doSubstitution(InternalEvent ievent, DeserializedEvent devent) {
    try {
      devent.setField(this.key, this.value);
    } catch (FieldNotFoundException e) {
      if (this.failDstNotFound) {
        throw new OperationException(e);
      }
    }
  }
}
