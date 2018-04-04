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

package com.nextdoor.bender;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

import com.amazonaws.services.lambda.runtime.Context;
import com.nextdoor.bender.deserializer.DeserializedEvent;

/**
 * An object that abstracts away the events the function was triggered with and their origin.
 */
public class InternalEvent {
  private final String eventString;
  private final Context context;
  private final String eventSha1Hash;
  private final long arrivalTime;
  protected DeserializedEvent eventObj;
  private String serialized;
  private LinkedHashMap<String, String> partitions;
  private long eventTime;
  private final Map<String, Object> metadata = new HashMap<String, Object>(6);

  /**
   * @param eventString the raw string data of the event.
   * @param context lambda context of the function.
   * @param arrivalTime epoch time in MS when the event arrived.
   */
  public InternalEvent(String eventString, Context context, long arrivalTime) {
    this.eventString = eventString;
    this.context = context;
    this.eventSha1Hash = DigestUtils.sha1Hex(this.eventString);
    this.arrivalTime = arrivalTime;
    this.eventTime = arrivalTime;

    this.metadata.put("eventEpochMs", new Long(this.getEventTime()));
    this.metadata.put("arrivalEpochMs", new Long(this.arrivalTime));
    this.metadata.put("eventSha1Hash", this.getEventSha1Hash());
  }

  /**
   * @return Metadata about the InternalEvent. This is typically information that comes from the
   *         Lambda Event that invoked the function.
   */
  public Map<String, Object> getEventMetadata() {
    return this.metadata;
  }

  /**
   * Allows classes that extend InternalEvent to add their own metadata.
   */
  protected void addMetadata(String key, Object value) {
    this.metadata.put(key, value);
  }

  /**
   * @return time the event arrived
   */
  public long getArrivalTime() {
    return this.arrivalTime;
  }

  /**
   * @return original event string.
   */
  public String getEventString() {
    return eventString;
  }

  /**
   * @return eventObj after it has been deserialized. Could be null if setEventObj was not called by
   *         {@link com.nextdoor.bender.deserializer.Deserializer}.
   */
  public DeserializedEvent getEventObj() {
    return this.eventObj;
  }

  /**
   * @param eventObj event after it has gone through the
   *        {@link com.nextdoor.bender.deserializer.Deserializer}.
   */
  public void setEventObj(DeserializedEvent eventObj) {
    this.eventObj = eventObj;
  }

  /**
   * @return context that the lambda function is called with in the
   *         {@link com.nextdoor.bender.handler.Handler#handler(Object, Context)}.
   */
  public Context getCtx() {
    return this.context;
  }

  /**
   * @return SHA1 hash of the original event String.
   */
  public String getEventSha1Hash() {
    return eventSha1Hash;
  }

  /**
   * @return string version of the event after it has been wrapped and serialized.
   */
  public String getSerialized() {
    return serialized;
  }

  /**
   * @param serialized String version of the event after it has been wrapped and serialized.
   */
  public void setSerialized(String serialized) {
    this.serialized = serialized;
  }

  public void setPartitions(LinkedHashMap<String, String> partitions) {
    this.partitions = partitions;
  }

  /**
   * @return ordered key value mapping of partitions to values. Maybe null if setPartitions not
   *         called.
   */
  public LinkedHashMap<String, String> getPartitions() {
    return partitions;
  }

  public long getEventTime() {
    return eventTime;
  }

  public void setEventTime(long eventTime) {
    this.eventTime = eventTime;
    this.addMetadata("eventEpochMs", new Long(eventTime));
    this.addMetadata("sourceLagMs", new Long(System.currentTimeMillis() - this.getEventTime()));
  }
}
