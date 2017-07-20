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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

import com.google.gson.JsonSyntaxException;
import com.nextdoor.bender.monitoring.Stat;
import com.nextdoor.bender.testutils.DummyMutatorHelper.DummyMutator;
import com.nextdoor.bender.testutils.DummyMutatorHelper.DummyMutatorFactory;

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

    processor.setRuntimeStat(runtimeStat);
    processor.setSuccessCountStat(successStat);
    processor.setErrorCountStat(errorStat);

    try {
      processor.mutate(null);
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

    doThrow(new UnsupportedMutationException("test")).when(mutator).mutateEvent(null);

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
      processor.mutate(null);
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
