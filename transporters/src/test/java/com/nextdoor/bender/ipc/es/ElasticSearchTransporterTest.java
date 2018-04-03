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

package com.nextdoor.bender.ipc.es;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.nextdoor.bender.ipc.TransportException;

public class ElasticSearchTransporterTest {

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
    byte[] respPayload = "{\"errors\":false}".getBytes(StandardCharsets.UTF_8);
    byte[] payload = "foo".getBytes();

    HttpClient client =
        getMockClientWithResponse(respPayload, ContentType.APPLICATION_JSON, HttpStatus.SC_OK);
    ElasticSearchTransport transport = new ElasticSearchTransport(client, false);

    transport.sendBatch(payload);
  }

  @Test(expected = TransportException.class)
  public void testNotOkResponse() throws TransportException, IOException {
    byte[] respPayload = "{\"foo\": \"bar\"}".getBytes(StandardCharsets.UTF_8);

    HttpClient client = getMockClientWithResponse(respPayload, ContentType.APPLICATION_JSON,
        HttpStatus.SC_INTERNAL_SERVER_ERROR);
    ElasticSearchTransport transport = new ElasticSearchTransport(client, false);

    transport.sendBatch("foo".getBytes());
  }

  @Test
  public void testNoErrorsResponse() throws TransportException, IOException {
    EsResponse resp = new EsResponse();
    resp.errors = false;
    byte[] respPayload = "{\"errors\":false}".getBytes(StandardCharsets.UTF_8);

    HttpClient client =
        getMockClientWithResponse(respPayload, ContentType.APPLICATION_JSON, HttpStatus.SC_OK);
    ElasticSearchTransport transport = new ElasticSearchTransport(client, false);

    transport.sendBatch("foo".getBytes());
  }

  private String getResponse() {
    EsResponse resp = new EsResponse();
    resp.errors = true;

    EsResponse.Item item = new EsResponse.Item();
    item.index = new EsResponse.Index();
    item.index.status = HttpStatus.SC_CONFLICT;
    item.index.error = new EsResponse.Error();
    item.index.error.reason = "unit test failure";
    item.index.error.type = "failure";
    item.index.error.caused_by = new EsResponse.Cause();
    item.index.error.caused_by.reason = "failure";
    item.index.error.caused_by.type = "internal";
    resp.items = Arrays.asList(item);

    Gson gson = new Gson();
    return gson.toJson(resp);
  }

  @Test(expected = TransportException.class)
  public void testErrorsResponse() throws TransportException, IOException {
    byte[] respPayload = getResponse().getBytes(StandardCharsets.UTF_8);

    HttpClient client =
        getMockClientWithResponse(respPayload, ContentType.APPLICATION_JSON, HttpStatus.SC_OK);
    ElasticSearchTransport transport = new ElasticSearchTransport(client, false);

    try {
      transport.sendBatch("foo".getBytes());
    } catch (Exception e) {
      assertEquals("es index failure count is 1", e.getCause().getMessage());
      throw e;
    }
  }

  @Test(expected = TransportException.class)
  public void testGzipErrorsResponse() throws TransportException, IOException {
    byte[] respPayload = getResponse().getBytes(StandardCharsets.UTF_8);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    GZIPOutputStream os = new GZIPOutputStream(baos);
    os.write(respPayload);
    os.close();
    byte[] compressedResponse = baos.toByteArray();

    HttpClient client =
        getMockClientWithResponse(compressedResponse, ContentType.DEFAULT_BINARY, HttpStatus.SC_OK);
    ElasticSearchTransport transport = new ElasticSearchTransport(client, true);

    try {
      transport.sendBatch("foo".getBytes());
    } catch (Exception e) {
      assertEquals("es call failed because expected failure", e.getCause().getMessage());
      throw e;
    }
  }

  @Test(expected = TransportException.class)
  public void testRetries() throws Exception {
    byte[] respPayload = getResponse().getBytes(StandardCharsets.UTF_8);

    HttpClient client =
        getMockClientWithResponse(respPayload, ContentType.APPLICATION_JSON, HttpStatus.SC_OK);
    ElasticSearchTransport transport = new ElasticSearchTransport(client, "", false, 3, 10);

    try {
      transport.sendBatch("foo".getBytes());
    } catch (Exception e) {
      assertEquals("es index failure count is 1", e.getCause().getMessage());
      throw e;
    }
  }

  @Test(expected = JsonSyntaxException.class)
  public void testBadJson() throws Throwable {
    byte[] arr = "{".getBytes();
    HttpClient client =
        getMockClientWithResponse(arr, ContentType.DEFAULT_BINARY, HttpStatus.SC_OK);
    ElasticSearchTransport transport = new ElasticSearchTransport(client, true);

    try {
      transport.sendBatch("foo".getBytes());
    } catch (Exception e) {
      throw e.getCause().getCause();
    }
  }

  @Test
  public void testUncompressedContentType() {
    ElasticSearchTransport transport = new ElasticSearchTransport(null, false);
    assertEquals(ContentType.APPLICATION_JSON, transport.getUncompressedContentType());
  }
}
