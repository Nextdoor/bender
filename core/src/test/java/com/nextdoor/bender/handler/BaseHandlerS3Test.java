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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import com.amazonaws.services.s3.AmazonS3Client;
import com.nextdoor.bender.aws.S3MockClientFactory;
import com.nextdoor.bender.aws.TestContext;
import com.nextdoor.bender.handler.BaseHandlerTest.DummyEvent;
import com.nextdoor.bender.handler.BaseHandlerTest.DummyHandler;

public class BaseHandlerS3Test {
  private static final String S3_BUCKET = "foo";

  private S3MockClientFactory clientFactory;

  @Rule
  public final EnvironmentVariables envVars = new EnvironmentVariables();

  @Before
  public void setup() throws UnsupportedEncodingException, IOException {
    /*
     * Patch the handler to use this test's factory which produces a mock client.
     */
    S3MockClientFactory f;
    try {
      f = new S3MockClientFactory();
    } catch (Exception e) {
      throw new RuntimeException("unable to start s3proxy", e);
    }

    AmazonS3Client client = f.newInstance();
    client.createBucket(S3_BUCKET);
    this.clientFactory = f;

    /*
     * Upload config file
     */
    String payload = IOUtils.toString(new InputStreamReader(
        this.getClass().getResourceAsStream("/config/handler_config.json"), "UTF-8"));
    client.putObject(S3_BUCKET, "bender/config.json", payload);

    /*
     * Export config file as env var
     */
    envVars.set("BENDER_CONFIG", "s3://" + S3_BUCKET + "/bender/config.json");
  }

  @After
  public void teardown() {
	  if (this.clientFactory != null) {
		  this.clientFactory.shutdown();
	  }
  }

  @Test
  public void testS3Config() throws HandlerException {
    BaseHandler handler = new DummyHandler();
    handler.CONFIG_FILE = null;
    handler.s3ClientFactory = this.clientFactory;

    List<DummyEvent> events = new ArrayList<DummyEvent>(1);

    TestContext context = new TestContext();
    context.setInvokedFunctionArn("arn:aws:lambda:us-east-1:123:function:test");
    handler.handler(events, context);

    assertEquals("s3://" + S3_BUCKET + "/bender/config.json", handler.config.getConfigFile());
    assertEquals("Test Events", handler.config.getSources().get(0).getName());
  }
}
