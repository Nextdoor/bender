package com.nextdoor.bender.time;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializationException;
import com.nextdoor.bender.operation.Operation;
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

  @Override
  public InternalEvent perform(InternalEvent ievent) {
    String field = ievent.getEventObj().getField(timeField);
    ievent.setEventTime(getTimestamp(field, timeFieldType));

    return ievent;
  }
}
