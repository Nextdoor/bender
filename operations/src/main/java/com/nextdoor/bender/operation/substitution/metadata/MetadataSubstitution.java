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

package com.nextdoor.bender.operation.substitution.metadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.FieldNotFoundException;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.operation.substitution.Substitution;

public class MetadataSubstitution extends Substitution {
  private final String key;
  private final List<String> includes;
  private final List<String> excludes;
  private final boolean failDstNotFound;

  public MetadataSubstitution(String key, List<String> includes, List<String> excludes,
      boolean failDstNotFound) {
    this.key = key;
    this.includes = includes;
    this.excludes = excludes;
    this.failDstNotFound = failDstNotFound;
  }

  @Override
  protected void doSubstitution(InternalEvent ievent, DeserializedEvent devent,
      Map<String, Object> nested) {
    Map<String, Object> metadata = getMetadata(ievent);
    nested.put(this.key, metadata);
  }

  @Override
  protected void doSubstitution(InternalEvent ievent, DeserializedEvent devent) {
    Map<String, Object> metadata = getMetadata(ievent);

    try {
      devent.setField(this.key, metadata);
    } catch (FieldNotFoundException e) {
      if (this.failDstNotFound) {
        throw new OperationException(e);
      }
    }
  }

  private Map<String, Object> getMetadata(InternalEvent ievent) {
    Map<String, Object> metadata = new HashMap<>(ievent.getEventMetadata());

    if (!this.includes.isEmpty()) {
      metadata.keySet().retainAll(this.includes);
    }

    this.excludes.forEach(exclude -> {
      metadata.remove(exclude);
    });

    return metadata;
  }
}
