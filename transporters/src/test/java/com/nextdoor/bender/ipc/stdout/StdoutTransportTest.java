/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright 2018 Nextdoor.com, Inc
 */
package com.nextdoor.bender.ipc.stdout;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.aws.TestContext;
import com.nextdoor.bender.ipc.generic.GenericTransportBuffer;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static junit.framework.TestCase.assertEquals;

public class StdoutTransportTest {

  @Test
  public void testStdout() throws IllegalStateException, IOException {
    ByteArrayOutputStream bo = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bo));

    StdoutTransport transport = new StdoutTransport();
    GenericTransportBuffer buf = new GenericTransportBuffer(1, false, new StdoutTransportSerializer());
    InternalEvent event = new InternalEvent("junk", new TestContext(), 123);
    event.setSerialized("junk");
    buf.add(event);
    transport.sendBatch(buf);

    bo.flush();
    String allWrittenLines = new String(bo.toByteArray());

    assertEquals("junk\n", allWrittenLines);
  }
}
