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
 * Copyright 2016 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.deserializer;

import java.util.NoSuchElementException;

public interface DeserializedEvent {
  /**
   * Retrieves deserialized object. Could be "this" object itself.
   *
   * @return the deserialized event object as created by the deserializer.
   */
  public Object getPayload();

  /**
   * Changes the payload object.
   *
   * @param object new Object to set payload to. This is typically done by the mutator.
   */
  public void setPayload(Object object);

  /**
   * Retrieves a field from the deserialized object.
   *
   * @param fieldName field to lookup.
   * @return String value of the field.
   * @throws NoSuchElementException when the field does not exist.
   */
  public String getField(String fieldName) throws NoSuchElementException;
}
