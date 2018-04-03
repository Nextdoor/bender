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

package com.nextdoor.bender.serializer;


import com.nextdoor.bender.monitoring.MonitoredProcess;
import com.nextdoor.bender.utils.ReflectionUtils;

/**
 * Wrapper around {@link Serializer} that keeps timing information on how long it takes to serialize
 * events and handles error cases.
 */
public class SerializerProcessor extends MonitoredProcess {
  private Serializer serializer;

  public SerializerProcessor(Serializer serializer) {
    super(serializer.getClass());
    this.serializer = serializer;
  }

  public SerializerProcessor(Class<? extends SerializerFactory> clazz) {
    this(ReflectionUtils.newInstance(clazz).newInstance());
  }

  public String serialize(Object obj) throws SerializationException {
    this.getRuntimeStat().start();
    String serialized;
    try {
      serialized = this.serializer.serialize(obj);
    } catch (Exception e) {
      this.getErrorCountStat().increment();
      throw new SerializationException(e);
    } finally {
      this.getRuntimeStat().stop();
    }

    this.getSuccessCountStat().increment();

    return serialized;
  }

  public Serializer getSerializer() {
    return this.serializer;
  }

  public void setSerializer(Serializer serializer) {
    this.serializer = serializer;
  }
}
