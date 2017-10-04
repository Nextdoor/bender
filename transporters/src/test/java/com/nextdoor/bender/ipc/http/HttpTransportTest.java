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

package com.nextdoor.bender.ipc.http;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.ipc.http.HttpTransport;

public class HttpTransportTest {

  private HttpClient getMockClientWithResponse(byte[] respPayload, ContentType contentType,
      int status) throws IOException {
    HttpClient mockClient = mock(HttpClient.class);
    HttpResponse mockResponse = mock(HttpResponse.class);

    StatusLine mockStatusLine = mock(StatusLine.class);
    doReturn("expected failure").when(mockStatusLine).getReasonPhrase();
    doReturn(status).when(mockStatusLine).getStatusCode();
    doReturn(mockStatusLine).when(mockResponse).getStatusLine();

    HttpEntity entity = new ByteArrayEntity(respPayload, contentType);

    doReturn(entity).when(mockResponse).getEntity();

    doReturn(mockResponse).when(mockClient).execute(any(HttpPost.class));
    return mockClient;
  }

  @Test
  public void testOkResponse() throws TransportException, IOException {
    byte[] respPayload = "{}".getBytes(StandardCharsets.UTF_8);
    byte[] payload = "foo".getBytes();

    HttpClient client =
        getMockClientWithResponse(respPayload, ContentType.APPLICATION_JSON, HttpStatus.SC_OK);
    HttpTransport transport = new HttpTransport(client, "", false, 1, 1);
    transport.sendBatch(payload);
  }

  @Test(expected = TransportException.class)
  public void testNotOkResponse() throws TransportException, IOException {
    byte[] respPayload = "{\"foo\": \"bar\"}".getBytes(StandardCharsets.UTF_8);

    HttpClient client = getMockClientWithResponse(respPayload, ContentType.APPLICATION_JSON,
        HttpStatus.SC_INTERNAL_SERVER_ERROR);
    HttpTransport transport = new HttpTransport(client, "", false, 1, 1);

    transport.sendBatch("foo".getBytes());
  }

  @Test
  public void testNoErrorsResponse() throws TransportException, IOException {
    byte[] respPayload = "{}".getBytes(StandardCharsets.UTF_8);

    HttpClient client =
        getMockClientWithResponse(respPayload, ContentType.APPLICATION_JSON, HttpStatus.SC_OK);
    HttpTransport transport = new HttpTransport(client, "", false, 1, 1);

    transport.sendBatch("foo".getBytes());
  }

  @Test(expected = TransportException.class)
  public void testErrorsResponse() throws TransportException, IOException {
    byte[] respPayload = "resp".getBytes(StandardCharsets.UTF_8);

    HttpClient client = getMockClientWithResponse(respPayload, ContentType.APPLICATION_JSON,
        HttpStatus.SC_INTERNAL_SERVER_ERROR);
    HttpTransport transport = new HttpTransport(client, "", false, 1, 1);

    try {
      transport.sendBatch("foo".getBytes());
    } catch (Exception e) {
      assertEquals("http transport call failed because resp", e.getCause().getMessage());
      throw e;
    }
  }

  @Test(expected = TransportException.class)
  public void testGzipErrorsResponse() throws TransportException, IOException {
    byte[] respPayload = "gzip resp".getBytes(StandardCharsets.UTF_8);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    GZIPOutputStream os = new GZIPOutputStream(baos);
    os.write(respPayload);
    os.close();
    byte[] compressedResponse = baos.toByteArray();

    HttpClient client = getMockClientWithResponse(compressedResponse, ContentType.DEFAULT_BINARY,
        HttpStatus.SC_INTERNAL_SERVER_ERROR);
    HttpTransport transport = new HttpTransport(client, "", true, 1, 1);

    try {
      transport.sendBatch("foo".getBytes());
    } catch (Exception e) {
      assertEquals("http transport call failed because gzip resp", e.getCause().getMessage());
      throw e;
    }
  }

  @Test(expected = TransportException.class)
  public void testRetries() throws Exception {
    byte[] respPayload = "resp".getBytes(StandardCharsets.UTF_8);

    HttpClient client = getMockClientWithResponse(respPayload, ContentType.APPLICATION_JSON,
        HttpStatus.SC_INTERNAL_SERVER_ERROR);
    HttpTransport transport = new HttpTransport(client, "", false, 3, 10);

    try {
      transport.sendBatch("foo".getBytes());
    } catch (Exception e) {
      assertEquals("http transport call failed because resp", e.getCause().getMessage());
      throw e;
    }
  }

  @Test(expected = IOException.class)
  public void testIOException() throws Throwable {
    byte[] arr = "{".getBytes();
    HttpClient client = getMockClientWithResponse(arr, ContentType.DEFAULT_BINARY,
        HttpStatus.SC_INTERNAL_SERVER_ERROR);

    doThrow(new IOException()).when(client).execute(any(HttpPost.class));

    HttpTransport transport = new HttpTransport(client, "", true, 3, 1);

    try {
      transport.sendBatch("foo".getBytes());
    } catch (Exception e) {
      throw e.getCause().getCause();
    }
  }

  @Test
  public void testHttpPostUrl() throws TransportException, IOException {
    byte[] respPayload = "{}".getBytes(StandardCharsets.UTF_8);
    byte[] payload = "foo".getBytes();
    String url = "https://localhost:443/foo";

    HttpClient client =
        getMockClientWithResponse(respPayload, ContentType.APPLICATION_JSON, HttpStatus.SC_OK);
    HttpTransport transport = spy(new HttpTransport(client, url, false, 1, 1));
    transport.sendBatch(payload);

    ArgumentCaptor<HttpPost> captor = ArgumentCaptor.forClass(HttpPost.class);
    verify(client, times(1)).execute(captor.capture());

    assertEquals(url, captor.getValue().getURI().toString());
  }

  @Test
  public void testHttpPostGzipHeader() throws TransportException, IOException {
    byte[] respPayload = "{}".getBytes(StandardCharsets.UTF_8);
    byte[] payload = "foo".getBytes();
    String url = "https://localhost:443/foo";

    HttpClient client =
        getMockClientWithResponse(respPayload, ContentType.APPLICATION_JSON, HttpStatus.SC_OK);
    HttpTransport transport = spy(new HttpTransport(client, url, true, 1, 1));
    transport.sendBatch(payload);

    ArgumentCaptor<HttpPost> captor = ArgumentCaptor.forClass(HttpPost.class);
    verify(client, times(1)).execute(captor.capture());

    assertEquals("gzip", captor.getValue().getFirstHeader("Accept-Encoding").getValue());
  }
}
