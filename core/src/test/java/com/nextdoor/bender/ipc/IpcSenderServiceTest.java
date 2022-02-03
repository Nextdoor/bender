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
 * Copyright 2018 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.ipc;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.monitoring.Stat;

public class IpcSenderServiceTest {
  public static class DummyTransporter implements UnpartitionedTransport {

    @Override
    public void sendBatch(TransportBuffer buffer) throws TransportException {

    }
  }

  public static class DummyTransportBuffer implements TransportBuffer {

    protected ArrayList<InternalEvent> buffer = new ArrayList<>(5);

    public DummyTransportBuffer() {}

    public DummyTransportBuffer(List<InternalEvent> events) {
      buffer.addAll(events);
    }

    @Override
    public boolean add(InternalEvent ievent) throws IllegalStateException, IOException {
      if (buffer.size() == 5) {
        throw new IllegalStateException("full");
      }
      buffer.add(ievent);
      return false;
    }

    @Override
    public Object getInternalBuffer() {
      return buffer;
    }

    @Override
    public boolean isEmpty() {
      return buffer.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
      boolean ret = ((DummyTransportBuffer) other).buffer.equals(this.buffer);
      return ret;
    }

    @Override
    public void close() {}

    @Override
    public void clear() {
      // Do not clear buffer as unit tests rely on buffer to be populated
    }
  }

  public static class DummyTransporterFactory implements TransportFactory {
    public DummyTransporter transporter;

    @Override
    public UnpartitionedTransport newInstance() {
      return transporter;
    }

    @Override
    public void setConf(AbstractConfig config) {}

    @Override
    public void close() {}

    @Override
    public TransportBuffer newTransportBuffer() throws TransportException {
      return new DummyTransportBuffer();
    }

    @Override
    public int getMaxThreads() {
      return 1;
    }

    @Override
    public Class<DummyTransporter> getChildClass() {
      return DummyTransporter.class;
    }
  }

  public static class DummyPartTransporter implements PartitionedTransport {
    @Override
    public void sendBatch(TransportBuffer buffer, LinkedHashMap partitions, Context context)
        throws TransportException {

    }
  }

  public static class DummyPartTransporterFactory implements TransportFactory {
    public DummyPartTransporter transporter;

    @Override
    public DummyPartTransporter newInstance() {
      return transporter;
    }

    @Override
    public void setConf(AbstractConfig config) {}

    @Override
    public void close() {}

    @Override
    public TransportBuffer newTransportBuffer() throws TransportException {
      return new DummyTransportBuffer();
    }

    @Override
    public int getMaxThreads() {
      return 1;
    }

    @Override
    public Class<DummyPartTransporter> getChildClass() {
      return DummyPartTransporter.class;
    }
  }

  @Test
  public void testBufferFlush() throws InterruptedException, TransportException {
    /*
     * Create a transport factory and dummy transporter
     */
    DummyTransporter mockDummyTransporter = mock(DummyTransporter.class);
    DummyTransporterFactory tfactory = new DummyTransporterFactory();
    tfactory.transporter = mockDummyTransporter;
    IpcSenderService ipc = new IpcSenderService(tfactory);

    /*
     * Create 12 events. Every 5 adds a send should happen.
     */
    ArrayList<InternalEvent> sent = new ArrayList<>();
    for (int i = 0; i < 12; i++) {
      InternalEvent ie = new DummyEvent("" + i, 0);
      ie.setPartitions(new LinkedHashMap(0));
      sent.add(ie);
      ipc.add(ie);
    }

    /*
     * Flush anything left in the buffer
     */
    ipc.flush();

    /*
     * Verify expected calls
     */
    verify(mockDummyTransporter, times(1)).sendBatch(new DummyTransportBuffer(sent.subList(0, 5)));
    verify(mockDummyTransporter, times(1)).sendBatch(new DummyTransportBuffer(sent.subList(5, 10)));
    verify(mockDummyTransporter, times(1))
        .sendBatch(new DummyTransportBuffer(sent.subList(10, 12)));
  }

  @Test
  public void testStatsLogging() throws InstantiationException, IllegalAccessException,
      InterruptedException, TransportException {
    DummyTransporterFactory tfactory = new DummyTransporterFactory();
    tfactory.transporter = new DummyTransporter();
    IpcSenderService ipc = new IpcSenderService(tfactory);

    /*
     * Mock the Stat object
     */
    Stat runtimeStat = mock(Stat.class);

    Stat forkedRuntimeStat = mock(Stat.class);
    when(runtimeStat.fork()).thenReturn(forkedRuntimeStat);
    Stat successStat = mock(Stat.class);
    Stat errorStat = mock(Stat.class);

    ipc.setRuntimeStat(runtimeStat);
    ipc.setSuccessCountStat(successStat);
    ipc.setErrorCountStat(errorStat);

    /*
     * Every 5 adds a send should happen.
     */
    for (int i = 0; i < 12; i++) {
      ipc.add(mock(InternalEvent.class));
    }

    ipc.flush();

    /*
     * Service should create three runnables and each will fork the stats object and call start,
     * stop, and increment success. On service shutdown the join method should only be called once.
     */
    verify(runtimeStat, times(3)).fork();
    verify(forkedRuntimeStat, times(3)).start();
    verify(forkedRuntimeStat, times(3)).stop();

    verify(runtimeStat, times(1)).join();
    verify(successStat, times(3)).increment();
    verify(errorStat, never()).increment();
  }

  @Test
  public void testStatsLoggingOnError() throws InstantiationException, IllegalAccessException,
      InterruptedException, TransportException {
    DummyTransporter mockDummyTransporter = mock(DummyTransporter.class);
    DummyTransporterFactory tfactory = new DummyTransporterFactory();
    tfactory.transporter = mockDummyTransporter;
    doThrow(new TransportException("expected exception in test")).when(mockDummyTransporter)
        .sendBatch(any(DummyTransportBuffer.class));
    IpcSenderService ipc = new IpcSenderService(tfactory);

    /*
     * Mock the Stat object
     */
    Stat successStat = mock(Stat.class);
    Stat errorStat = mock(Stat.class);

    ipc.setSuccessCountStat(successStat);
    ipc.setErrorCountStat(errorStat);

    /*
     * Every 5 adds a send should happen.
     */
    for (int i = 0; i < 5; i++) {
      ipc.add(mock(InternalEvent.class));
    }

    try {
      ipc.flush();
    } catch (TransportException e) {
      // expected
    }

    /*
     * The sendBatch method will be called twice and each will throw an error. Verify that error
     * counting happens as expected.
     */
    verify(successStat, never()).increment();
    verify(errorStat, times(1)).increment();
  }

  @Test(expected = TransportException.class)
  public void testExceptionEscalation() throws TransportException, InterruptedException {
    DummyTransporter mockDummyTransporter = mock(DummyTransporter.class);
    DummyTransporterFactory tfactory = spy(new DummyTransporterFactory());
    tfactory.transporter = mockDummyTransporter;
    doThrow(new TransportException("expected exception in test")).when(mockDummyTransporter)
        .sendBatch(any(DummyTransportBuffer.class));
    IpcSenderService ipc = new IpcSenderService(tfactory);

    ipc.add(mock(InternalEvent.class));

    /*
     * Expect shutdown to throw a TransportException when a child thread also throws as
     * TransportException
     *
     * flush() is required to be called since we added a single event only and the dummy transport
     * buffer only empties the buffer with 5 events stored
     */
    try {
      ipc.flush();
    } catch (TransportException e) {
      // we expect that the factory is closed since the exception check is done at the end
      verify(tfactory).close();
      throw e;
    }

  }

  @Test(expected = TransportException.class)
  public void testThreadExceptionDuringAdd() throws TransportException, InterruptedException {
    DummyTransporter mockDummyTransporter = mock(DummyTransporter.class);
    DummyTransporterFactory tfactory = new DummyTransporterFactory();
    tfactory.transporter = mockDummyTransporter;

    doThrow(new TransportException("expected exception in test")).when(mockDummyTransporter)
            .sendBatch(any(DummyTransportBuffer.class));

    IpcSenderService ipc = new IpcSenderService(tfactory);
    // add 5 events to fill up the buffer so the next will result in a sendBatch()
    for (int i = 0; i < 5; i++) {
      ipc.add(mock(InternalEvent.class));
    }

    // this will trigger the sendBatch exception above
    ipc.add(mock(InternalEvent.class));

    Thread.sleep(5); // lets thread from last event complete and throw exception
    ipc.add(mock(InternalEvent.class)); // add() checks at start for any threads that threw exceptions
  }

  private static class DummyEvent extends InternalEvent {
    private LinkedHashMap<String, String> partitions;

    public DummyEvent(String record, long timestamp, LinkedHashMap<String, String> partitions) {
      super(record, null, timestamp);
      this.partitions = partitions;
    }

    public DummyEvent(String record, long timestamp) {
      super(record, null, timestamp);
      this.partitions = new LinkedHashMap<>(0);
    }

    @Override
    public LinkedHashMap<String, String> getPartitions() {
      return this.partitions;
    }
  }

  @Test
  public void testPartitionedTransport() throws InterruptedException, TransportException {
    /*
     * Create a transport factory and dummy transporter
     */
    DummyTransporter mockDummyTransporter = mock(DummyTransporter.class);
    DummyTransporterFactory tfactory = new DummyTransporterFactory();
    tfactory.transporter = mockDummyTransporter;
    IpcSenderService ipc = new IpcSenderService(tfactory);

    ArrayList<InternalEvent> sent1 = new ArrayList<>();
    ArrayList<InternalEvent> sent2 = new ArrayList<>();

    /*
     * Write 3 events to partition test1
     */
    LinkedHashMap<String, String> part1 = new LinkedHashMap<>(1);
    part1.put("p1", "test1");
    for (int i = 0; i < 3; i++) {
      InternalEvent ie = new DummyEvent("" + i, 0, part1);
      sent1.add(ie);
      ipc.add(ie);
    }

    /*
     * Write 5 events to partition test2
     */
    LinkedHashMap<String, String> part2 = new LinkedHashMap<>(1);
    part2.put("p1", "test2");
    for (int i = 0; i < 5; i++) {
      InternalEvent ie = new DummyEvent("" + i, 0, part2);
      sent2.add(ie);
      ipc.add(ie);
    }

    /*
     * Write 2 events to partition test1 again
     */
    LinkedHashMap<String, String> part3 = new LinkedHashMap<>(1);
    part3.put("p1", "test1");
    for (int i = 0; i < 2; i++) {
      InternalEvent ie = new DummyEvent("" + i, 0, part3);
      sent1.add(ie);
      ipc.add(ie);
    }

    /*
     * Flush anything left in the buffer
     */
    ipc.flush();

    /*
     * Verify expected calls
     */
    verify(mockDummyTransporter, times(1)).sendBatch(new DummyTransportBuffer(sent1.subList(0, 5)));
    verify(mockDummyTransporter, times(1)).sendBatch(new DummyTransportBuffer(sent2.subList(0, 5)));
  }
}
