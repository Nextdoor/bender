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

import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.jayway.jsonpath.InvalidPathException;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.operation.Operation;
import com.nextdoor.bender.operation.OperationException;

/**
 * This operation is used to remove certain events from the stream before continuing on to the
 * destination.
 *
 * Each event is assessed by applying the path {@link com.jayway.jsonpath.JsonPath} to its payload
 * {@link com.google.gson.JsonObject} and matching the value against a regex {@link Pattern}.
 *
 * The match {@link Boolean} is used to determine which events to filter out.
 * If match is true, events with matching values are filtered out.
 * If match is false, events without matching values are filtered out.
 */
public class FilterOperation implements Operation {
  private final String regex;
  private final String path;
  private final Boolean match;

  public FilterOperation(String regex, String path, Boolean match) {
    this.regex = regex;
    this.path = path;
    this.match = match;
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
      found = (field != null) && Pattern.matches(regex, field);
    } catch (InvalidPathException e) {
      throw new OperationException("Invalid JsonPath");
    } catch (PatternSyntaxException e) {
      throw new OperationException("Invalid regex");
    } catch (NoSuchElementException e) {
      found = false;
    }

    return match == found;
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
