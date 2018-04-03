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

package com.nextdoor.bender.time;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.operation.Operation;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.time.TimeOperationConfig.TimeFieldType;
import com.nextdoor.bender.utils.Time;

public class TimeOperation implements Operation {
  private final String timeField;
  private final TimeFieldType timeFieldType;

  public TimeOperation(String timeField, TimeFieldType timeFieldType) {
    this.timeField = timeField;
    this.timeFieldType = timeFieldType;
  }

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
        throw new OperationException("unsupported TimeFieldType");
    }

    /*
     * Sanity Check
     */
    try {
      return Time.toMilliseconds(ts);
    } catch (IllegalArgumentException e) {
      throw new OperationException(e);
    }
  }

  @Override
  public InternalEvent perform(InternalEvent ievent) {
    String field = ievent.getEventObj().getField(timeField);

    /*
     * Filter out events without a time field
     */
    if (field == null) {
      throw new OperationException(
          "time field " + timeField + " returned null from " + ievent.getEventString());
    }

    ievent.setEventTime(getTimestamp(field, timeFieldType));

    return ievent;
  }
}
