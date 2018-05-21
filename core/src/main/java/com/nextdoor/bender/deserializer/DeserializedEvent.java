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
 * Copyright 2017 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.deserializer;

import org.apache.commons.lang3.NotImplementedException;

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
   * @return Value of the field.
   * @throws FieldNotFoundException when the field does not exist.
   */
  public Object getField(String fieldName) throws FieldNotFoundException;

  /**
   * Retrieves a field from the deserialized object as a String. Note this may rely on
   * the object's implementation of toString().
   *
   * @param fieldName field to lookup.
   * @return String value of the field.
   * @throws FieldNotFoundException when the field does not exist.
   */
  public String getFieldAsString(String fieldName) throws FieldNotFoundException;

  /**
   * Sets a field in the deserialized object.
   *
   * @param fieldName name of the field to set.
   * @param value of the field.
   * @throws NotImplementedException if the deserialized event does not support this action.
   * @throws FieldNotFoundException if field was unable to be set. Most likely due to payload
   *         being null.
   */
  public void setField(String fieldName, Object value)
      throws NotImplementedException, FieldNotFoundException;

  /**
   * Sets a field in the deserialized object.
   *
   * @param fieldName name of the field to set.
   * @return Value of the field
   * @throws FieldNotFoundException if field does not exist.
   */
  public Object removeField(String fieldName) throws FieldNotFoundException;

  /**
   * Provides a deep copy of this object as well as the payload.
   *
   * @return copy of this object.
   */
  public DeserializedEvent copy();
}
