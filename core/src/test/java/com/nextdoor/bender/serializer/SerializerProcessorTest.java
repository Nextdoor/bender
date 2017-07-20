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

package com.nextdoor.bender.serializer;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import com.nextdoor.bender.monitoring.Stat;
import com.nextdoor.bender.mutator.UnsupportedMutationException;
import com.nextdoor.bender.testutils.DummySerializerHelper.DummySerializer;

public class SerializerProcessorTest {

  @Test
  public void testStatsLogging() throws SerializationException {
    DummySerializer serializer = new DummySerializer();
    SerializerProcessor processor = new SerializerProcessor(serializer);

    /*
     * Mock the Stat object
     */
    Stat runtimeStat = mock(Stat.class);
    Stat successStat = mock(Stat.class);
    Stat errorStat = mock(Stat.class);

    processor.setRuntimeStat(runtimeStat);
    processor.setSuccessCountStat(successStat);
    processor.setErrorCountStat(errorStat);

    processor.serialize("foo");

    /*
     * Verify start, stop, increment success count, and never increment error count.
     */
    verify(runtimeStat, times(1)).start();
    verify(runtimeStat, times(1)).stop();
    verify(successStat, times(1)).increment();
    verify(errorStat, never()).increment();
  }

  @Test
  public void testStatsLoggingOnError() throws UnsupportedMutationException {
    DummySerializer serializer = mock(DummySerializer.class);
    SerializerProcessor processor = new SerializerProcessor(serializer);

    doThrow(new RuntimeException()).when(serializer).serialize("foo");

    /*
     * Mock the Stat object
     */
    Stat runtimeStat = mock(Stat.class);
    Stat successStat = mock(Stat.class);
    Stat errorStat = mock(Stat.class);

    processor.setRuntimeStat(runtimeStat);
    processor.setSuccessCountStat(successStat);
    processor.setErrorCountStat(errorStat);

    try {
      processor.serialize("foo");
    } catch (Exception e) {
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

  @Test
  public void testSerializeNull() throws UnsupportedMutationException {
    DummySerializer serializer = mock(DummySerializer.class);
    SerializerProcessor processor = new SerializerProcessor(serializer);

    doThrow(new RuntimeException()).when(serializer).serialize(null);

    /*
     * Mock the Stat object
     */
    Stat runtimeStat = mock(Stat.class);
    Stat successStat = mock(Stat.class);
    Stat errorStat = mock(Stat.class);

    processor.setRuntimeStat(runtimeStat);
    processor.setSuccessCountStat(successStat);
    processor.setErrorCountStat(errorStat);

    try {
      processor.serialize(null);
    } catch (Exception e) {
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
}
