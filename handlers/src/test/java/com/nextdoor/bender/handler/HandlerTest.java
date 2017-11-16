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

package com.nextdoor.bender.handler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nextdoor.bender.aws.TestContext;
import com.nextdoor.bender.ipc.IpcSenderService;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.testutils.DummyTransportHelper;

public abstract class HandlerTest<T> {

  public abstract Handler<T> getHandler();

  public abstract T getTestEvent() throws Exception;

  @Before
  public abstract void setup();

  @After
  public abstract void teardown();

  @Before
  public void before() {
    DummyTransportHelper.BufferedTransporter.output.clear();
  }

  @Test
  public void testBasicEndtoEnd() throws Exception {
    BaseHandler.CONFIG_FILE = "/com/nextdoor/bender/handler/config_unittest.json";

    TestContext ctx = new TestContext();
    ctx.setFunctionName("unittest");
    ctx.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test-function:staging");

    /*
     * Invoke handler
     */
    Handler<T> fhandler = getHandler();
    T event = getTestEvent();
    fhandler.handler(event, ctx);

    /*
     * Load output
     */
    assertEquals(1, DummyTransportHelper.BufferedTransporter.output.size());
    String actual = DummyTransportHelper.BufferedTransporter.output.get(0);

    /*
     * Load expected
     */
    String expected = IOUtils.toString(
        new InputStreamReader(this.getClass().getResourceAsStream("basic_output.json"), "UTF-8"));

    assertEquals(expected, actual);
  }

  @Test(expected = TransportException.class)
  public void testExceptionHandling() throws Throwable {
    BaseHandler.CONFIG_FILE = "/com/nextdoor/bender/handler/config_unittest.json";

    TestContext ctx = new TestContext();
    ctx.setFunctionName("unittest");
    ctx.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test-function:staging");

    /*
     * Invoke handler
     */
    BaseHandler<T> fhandler = (BaseHandler<T>) getHandler();
    fhandler.init(ctx);

    IpcSenderService ipcSpy = spy(fhandler.getIpcService());
    doThrow(new TransportException("expected")).when(ipcSpy).shutdown();
    fhandler.setIpcService(ipcSpy);

    T event = getTestEvent();

    try {
      fhandler.handler(event, ctx);
    } catch (Exception e) {
      throw e.getCause().getCause();
    }
  }
}
