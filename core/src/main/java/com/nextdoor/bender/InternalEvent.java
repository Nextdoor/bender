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

package com.nextdoor.bender;

import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import com.amazonaws.services.lambda.runtime.Context;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.DeserializedTimeSeriesEvent;
import com.nextdoor.bender.partition.PartitionSpec;

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
    return eventObj;
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
    return context;
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

  /**
   * @return epoch time in MS of when the event occurred. If not a time series event then arrival
   *         time is used.
   */
  public long getEventTimeMs() {
    if (this.eventObj != null && this.eventObj instanceof DeserializedTimeSeriesEvent) {
      return ((DeserializedTimeSeriesEvent) this.eventObj).getEpochTimeMs();
    }

    return this.arrivalTime;
  }

  /**
   * Provided a PartitionSpec this method attempts to retrieve each field from the deserialized
   * event object.
   * 
   * @param partitionSpecs list of PartitionSpec.
   */
  public void setPartitions(List<PartitionSpec> partitionSpecs) {
    int numPartSpecs = partitionSpecs.size();

    /*
     * Loop through each partition spec fetching the associated field from the event. Set to null if
     * field does not exist.
     */
    if (this.eventObj != null) {
      LinkedHashMap<String, String> partitions = new LinkedHashMap<String, String>(numPartSpecs);
      for (PartitionSpec spec : partitionSpecs) {
        String key = null;
        for (String source : spec.getSources()) {
          key = this.eventObj.getField(source);
          if (key != null) {
            break;
          }
        }

        partitions.put(spec.getName(), spec.interpret(key));
      }
      this.partitions = partitions;
      return;
    }

    /*
     * If deserialized event is null set all partitions to also null
     */
    LinkedHashMap<String, String> partitions = new LinkedHashMap<String, String>(numPartSpecs);
    for (PartitionSpec spec : partitionSpecs) {
      partitions.put(spec.getName(), null);
    }

    this.partitions = partitions;
  }

  /**
   * @return ordered key value mapping of partitions to values. Maybe null if setPartitions not
   *         called.
   */
  public LinkedHashMap<String, String> getPartitions() {
    return partitions;
  }
}
