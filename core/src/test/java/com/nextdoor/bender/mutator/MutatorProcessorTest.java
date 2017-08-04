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
 * Copyright $year Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.mutator;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.JsonSyntaxException;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.monitoring.Stat;
import com.nextdoor.bender.testutils.DummyDeserializerHelper;
import com.nextdoor.bender.testutils.DummyMutatorHelper.DummyMutator;
import com.nextdoor.bender.testutils.DummyMutatorHelper.DummyMutatorFactory;
import com.nextdoor.bender.utils.TestContext;

public class MutatorProcessorTest {

  @Test
  public void testStatsLogging() throws JsonSyntaxException, UnsupportedEncodingException,
      IOException, UnsupportedMutationException {
    DummyMutatorFactory mutatorFactory = new DummyMutatorFactory();

    MutatorProcessor processor = new MutatorProcessor(mutatorFactory);

    /*
     * Mock the Stat object
     */
    Stat runtimeStat = mock(Stat.class);
    Stat successStat = mock(Stat.class);
    Stat errorStat = mock(Stat.class);

    Context context = new TestContext();
    InternalEvent event = new InternalEvent("asdf", context, 0);
    List<InternalEvent> events = new ArrayList<>(1);
    events.add(event);

    processor.setRuntimeStat(runtimeStat);
    processor.setSuccessCountStat(successStat);
    processor.setErrorCountStat(errorStat);

    try {
      processor.mutate(events);
    } catch (UnsupportedMutationException e) {

    }

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
    DummyMutator mutator = mock(DummyMutator.class);
    DummyMutatorFactory mutatorFactory = new DummyMutatorFactory(mutator);
    MutatorProcessor processor = new MutatorProcessor(mutatorFactory);
    Context context = new TestContext();

    DeserializedEvent event = new DummyDeserializerHelper.DummyDeserializedEvent("bah");

    InternalEvent ievent = new InternalEvent("asdf", context, 0);
    ievent.setEventObj(event);
    List<InternalEvent> events = new ArrayList<>(1);
    events.add(ievent);

    doThrow(new UnsupportedMutationException("test")).when(mutator).mutateInternalEvent(events);

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
      processor.mutate(events);
    } catch (UnsupportedMutationException e) {
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
