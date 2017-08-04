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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;

import com.nextdoor.bender.monitoring.Stat;
import com.nextdoor.bender.partition.PartitionSpec;
import com.nextdoor.bender.testutils.DummyDeserializerHelper.DummyDeserializedEvent;
import com.nextdoor.bender.testutils.DummyDeserializerHelper.DummyDeserializer;;

public class DeserializerProcessorTest {

  @Test
  public void testStatsLogging() throws InstantiationException, IllegalAccessException {
    DeserializerProcessor deser = new DeserializerProcessor(new DummyDeserializer());

    /*
     * Mock the Stat object
     */
    Stat runtimeStat = mock(Stat.class);
    Stat successStat = mock(Stat.class);
    Stat errorStat = mock(Stat.class);

    deser.setRuntimeStat(runtimeStat);
    deser.setSuccessCountStat(successStat);
    deser.setErrorCountStat(errorStat);

    deser.deserialize("foo");

    /*
     * Verify start, stop, increment success count, and never increment error count.
     */
    verify(runtimeStat, times(1)).start();
    verify(runtimeStat, times(1)).stop();
    verify(successStat, times(1)).increment();
    verify(errorStat, never()).increment();
  }

  @Test
  public void testStatsLoggingOnError() {
    DummyDeserializer mockDeser = mock(DummyDeserializer.class);
    when(mockDeser.deserialize("foo"))
        .thenThrow(new DeserializationException(new RuntimeException("expected")));
    DeserializerProcessor deser = new DeserializerProcessor(mockDeser);

    /*
     * Mock the Stat object
     */
    Stat runtimeStat = mock(Stat.class);
    Stat successStat = mock(Stat.class);
    Stat errorStat = mock(Stat.class);

    deser.setRuntimeStat(runtimeStat);
    deser.setSuccessCountStat(successStat);
    deser.setErrorCountStat(errorStat);

    try {
      deser.deserialize("foo");
    } catch (DeserializationException e) {
      // expected
    }

    /*
     * Verify start, stop are called, increment error count and never increment success count.
     */
    verify(runtimeStat, times(1)).start();
    verify(runtimeStat, times(1)).stop();
    verify(successStat, never()).increment();
    verify(errorStat, times(1)).increment();
  }

  @Test(expected = RuntimeException.class)
  public void testOnUnexpectedError() {
    DummyDeserializer mockDeser = mock(DummyDeserializer.class);
    when(mockDeser.deserialize("foo")).thenThrow(new RuntimeException("unexpected"));
    DeserializerProcessor deser = new DeserializerProcessor(mockDeser);

    /*
     * Mock the Stat object
     */
    Stat runtimeStat = mock(Stat.class);
    Stat successStat = mock(Stat.class);
    Stat errorStat = mock(Stat.class);

    deser.setRuntimeStat(runtimeStat);
    deser.setSuccessCountStat(successStat);
    deser.setErrorCountStat(errorStat);

    try {
      deser.deserialize("foo");
    } catch (DeserializationException e) {
      // expected
    }
  }
}
