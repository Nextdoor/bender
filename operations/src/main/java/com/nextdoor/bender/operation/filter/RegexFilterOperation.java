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

package com.nextdoor.bender.operation.filter;

import java.util.regex.Pattern;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.FieldNotFoundException;
import com.nextdoor.bender.operation.Operation;
import com.nextdoor.bender.operation.OperationException;

/**
 * This operation is used to remove certain events from the stream before continuing on to the
 * destination.
 *
 * Each event is assessed by applying the path {@link com.jayway.jsonpath.JsonPath} to its payload
 * {@link com.google.gson.JsonObject} and matching the value against a regex {@link Pattern}.
 *
 * The exclude {@link Boolean} is used to determine which events to filter out.
 * If exclude is true, events with matching values are filtered out.
 * If exclude is false, events without matching values are filtered out.
 */
public class RegexFilterOperation implements Operation {
  private final Pattern pattern;
  private final String path;
  private final Boolean exclude;

  public RegexFilterOperation(Pattern pattern, String path, Boolean exclude) {
    this.pattern = pattern;
    this.path = path;
    this.exclude = exclude;
  }

  /**
   * Returns true if the event should be filtered out and false otherwise.
   *
   * @param devent {@link DeserializedEvent} has payload to filter against.
   */
  protected boolean filterEvent(DeserializedEvent devent) {
    boolean found;
    try {
      String field = devent.getFieldAsString(path);
      found = this.pattern.matcher(field).matches();
    } catch (FieldNotFoundException e) {
      found = false;
    }

    return exclude == found;
  }

  @Override
  public InternalEvent perform(InternalEvent ievent) {
    DeserializedEvent devent = ievent.getEventObj();
    if (devent == null) {
      throw new OperationException("Deserialized object is null");
    }
    // Returning null here causes the event to be filtered out.
    return filterEvent(devent) ? null : ievent;
  }
}
