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

package com.nextdoor.bender.deserializer.json;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jayway.jsonpath.JsonPath;
import com.nextdoor.bender.deserializer.DeserializationException;
import com.nextdoor.bender.deserializer.DeserializedTimeSeriesEvent;
import com.nextdoor.bender.utils.Time;

/**
 * Basic wrapper around an arbitrary JSON object which contains a time the event occurred.
 */
public class TimeSeriesJsonEvent extends GenericJsonEvent implements DeserializedTimeSeriesEvent {

  public static enum TimeFieldType {
    SECONDS, MILLISECONDS
  }

  final long timestamp;

  protected static long getTimestamp(String dvalue, TimeFieldType type) {
    long ts;
    switch (type) {
      case SECONDS:
        ts = (long) (Double.parseDouble(dvalue) * 1000);
        break;
      case MILLISECONDS:
        ts = (long) (Double.parseDouble(dvalue));
        break;
      default:
        throw new DeserializationException("unsupported TimeFieldType");
    }

    /*
     * Sanity Check
     */
    try {
      return Time.toMilliseconds(ts);
    } catch (IllegalArgumentException e) {
      throw new DeserializationException(e);
    }
  }

  protected TimeSeriesJsonEvent(GenericJsonEvent event, String timeKeyPath,
      TimeFieldType timeFieldType) {
    super((JsonObject) event.getPayload());

    Object obj = JsonPath.read((JsonObject) this.getPayload(), timeKeyPath);

    if (obj == null) {
      throw new DeserializationException(timeKeyPath +" is missing");
    }

    if (!(obj instanceof JsonPrimitive)) {
      throw new DeserializationException(timeKeyPath + " is not a primitive");
    }

    this.timestamp = getTimestamp(((JsonPrimitive) obj).getAsString(), timeFieldType);
  }

  @Override
  public long getEpochTimeMs() {
    return this.timestamp;
  }
}
