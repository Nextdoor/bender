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

package com.nextdoor.bender.deserializer;

import org.apache.log4j.Logger;

import com.nextdoor.bender.monitoring.MonitoredProcess;

/**
 * Wrapper around {@link Deserializer} that keeps timing information on how long it takes to
 * deserialize events and handles error cases.
 */
public class DeserializerProcessor extends MonitoredProcess {
  private static final Logger logger = Logger.getLogger(DeserializerProcessor.class);
  private Deserializer deser;

  public DeserializerProcessor(Deserializer deserializer) {
    super(deserializer.getClass());
    this.deser = deserializer;
    this.deser.init();
  }

  /**
   * Calls {@link com.nextdoor.bender.deserializer.Deserializer#deserialize(String)} and returns a
   * DeserializedEvent.
   *
   * @param eventString A plain text string which needs to be converted into a
   *        {@link DeserializedEvent}.
   * @return A DeserializedEvent if deserialization succeeded or null if it failed.
   */
  public DeserializedEvent deserialize(String eventString) {
    DeserializedEvent dEvent = null;
    this.getRuntimeStat().start();

    try {
      dEvent = this.deser.deserialize(eventString);
      this.getSuccessCountStat().increment();
    } catch (DeserializationException e) {
      logger.warn("failed to deserialize", e);
      this.getErrorCountStat().increment();
    } finally {
      this.getRuntimeStat().stop();
    }

    return dEvent;
  }

  public Deserializer getDeserializer() {
    return this.deser;
  }

  public void setDeserializer(Deserializer deserializer) {
    this.deser = deserializer;
  }

  public String toString() {
    return this.deser.getClass().getSimpleName();
  }
}
