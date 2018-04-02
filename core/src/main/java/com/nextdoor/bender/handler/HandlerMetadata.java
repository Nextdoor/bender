/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright $year Nextdoor.com, Inc
 */

package com.nextdoor.bender.handler;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

/**
 * The {@link HandlerMetadata} object is a simple Key/Value map that is used by {@Link
 * BaseHandler} to store information about how and when the function was invoked. This data is
 * either thrown away at the end of the execution, or is used later by our
 * {@link com.nextdoor.bender.wrapper.Wrapper} and
 * {@link com.nextdoor.bender.operation.Operation} classes to futher modify the payload before
 * its shipped.
 */
public class HandlerMetadata {
    private Map<String, Object> payload;
    private Boolean immutable = false;

    public HandlerMetadata() {
        this.payload = new HashMap<String, Object>();
    }

    public String getField(String fieldName) throws NoSuchElementException {
      if (this.payload == null) {
          throw new NoSuchElementException(fieldName + " is not in payload because payload is null");
      }

      Object o = this.payload.getOrDefault(fieldName, null);

      return o != null ? o.toString() : null;
    }

    public void setField(String fieldName, Object value) throws NotImplementedException {
      if (immutable) {
          throw new RuntimeException(this + " object has been marked immutable!");
      }
      this.payload.put(fieldName, value);
    }

    public List<String> getFields() {
      List<String> l = new ArrayList<String>(payload.keySet());
      return l;
    }

    public void setImmutable() {
        immutable = true;
    }
}
