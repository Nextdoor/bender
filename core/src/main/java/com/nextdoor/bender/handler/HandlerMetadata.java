/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * Copyright 2018 Nextdoor.com, Inc
 */

package com.nextdoor.bender.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * The {@link HandlerMetadata} object is a simple Key/Value map that is used by {@link
 * BaseHandler} to store information about how and when the function was invoked. This data is
 * either thrown away at the end of the execution, or is used later by our
 * {@link com.nextdoor.bender.wrapper.Wrapper} and
 * {@link com.nextdoor.bender.operation.Operation} classes to futher modify the payload before
 * its shipped.
 */
public class HandlerMetadata {
  private Map<String, Object> payload;
  private boolean immutable = false;

  public HandlerMetadata() {
        this.payload = new HashMap<String, Object>();
    }

  public Map<String, Object> getPayload() {
      return payload;
    }

  public Object getField(String fieldName) {
    Object o = this.payload.getOrDefault(fieldName, null);
    return o != null ? o.toString() : null;
  }

  public void setField(String fieldName, Object value) {
    if (immutable) {
        throw new RuntimeException(this + " object has been marked immutable!");
    }
    this.payload.put(fieldName, value);
  }

  public List<String> getFields() {
    return new ArrayList<String>(payload.keySet());
  }


  public boolean equals(Object o) {
    return this.payload.equals(o);
  }

  public int hashcode() {
    return this.payload.hashCode();
  }

  /*
   * This is a poor-mans immutable object. This method will flip the internal immutable bit,
   * which will disallow new fields to be added to the payload object. This does not. however,
   * prevent users from using getField() to pull down a mutable object and then modify it.
   *
   * For now, the objects submitted here are generally Strings/Ints, so they're Immutable
   * anyways. However in the future if someone were to submit a mutable object, it could be
   * modified by some bad code.
   */
  public void setImmutable() {
        immutable = true;
    }
}
