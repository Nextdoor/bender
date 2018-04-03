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
package com.nextdoor.bender.ipc.http;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.nextdoor.bender.auth.BasicHttpAuthConfig;
import com.nextdoor.bender.config.value.StringValueConfig;

public class HttpTransportFactoryTest {
  @Test
  public void testBasicAuthHeaders() {
    HttpTransportConfig config = new HttpTransportConfig();
    config.addHttpHeader("foo", "bar");

    BasicHttpAuthConfig auth = new BasicHttpAuthConfig();
    auth.setUsername("foo");
    auth.setPassword(new StringValueConfig("bar"));
    config.setBasicHttpAuth(auth);

    HttpTransportFactory factory = spy(new HttpTransportFactory());
    factory.setConf(config);

    ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
    verify(factory, times(1)).getClient(anyBoolean(), anyString(), captor.capture(), anyInt());

    Map<String, String> expected = new HashMap<String, String>();
    expected.put("foo", "bar");
    expected.put("Authorization", "Basic Zm9vOmJhcg==");

    assertEquals(expected, captor.getValue());
  }
}
