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
 * Copyright 2022 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.ipc.http2;

import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import com.evanlennick.retry4j.CallExecutor;
import com.evanlennick.retry4j.RetryConfig;
import com.evanlennick.retry4j.RetryConfigBuilder;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import com.nextdoor.bender.ipc.TransportBuffer;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.ipc.UnpartitionedTransport;
import com.nextdoor.bender.ipc.generic.GenericTransportBuffer;

/**
 * Generic HTTP Transport that supports HTTP/1.1 and HTTP/2. The HTTP client must be configured by
 * your transport factory.
 */
public class Http2Transport implements UnpartitionedTransport {
  private final CloseableHttpAsyncClient client;
  protected final boolean useGzip;
  private final long retryDelayMs;
  private final int retries;
  private final HttpHost target;
  private final URL url;
  private final long timeoutMs;

  private static final Logger logger = Logger.getLogger(Http2Transport.class);

  public Http2Transport(CloseableHttpAsyncClient client, URL url, boolean useGzip, int retries,
      long retryDelayMs, long timeoutMs) {
    this.client = client;
    this.url = url;
    this.useGzip = useGzip;
    this.retries = retries;
    this.retryDelayMs = retryDelayMs;
    this.timeoutMs = timeoutMs;
    this.target = new HttpHost(this.url.getProtocol(), this.url.getHost(), this.url.getPort());

    this.client.start();
  }

  protected ContentType getUncompressedContentType() {
    return ContentType.DEFAULT_TEXT;
  }

  protected SimpleHttpResponse execute(SimpleRequestBuilder rb, byte[] raw, ContentType type)
      throws TransportException {
    rb = rb.setBody(raw, getUncompressedContentType())
        .setRequestConfig(RequestConfig.custom().setResponseTimeout(this.timeoutMs, TimeUnit.MILLISECONDS).build());

    try {
      final Future<SimpleHttpResponse> future =
          client.execute(SimpleRequestProducer.create(rb.build()), SimpleResponseConsumer.create(),
              HttpClientContext.create(), new FutureCallback<SimpleHttpResponse>() {

                @Override
                public void completed(SimpleHttpResponse result) {}

                @Override
                public void failed(Exception ex) {}

                @Override
                public void cancelled() {}
              });
      return future.get();
    } catch (Exception e) {
      throw new TransportException("failed to make call", e);
    }
  }

  public void sendBatch(TransportBuffer buf) throws TransportException {
    GenericTransportBuffer buffer = (GenericTransportBuffer) buf;
    sendBatch(buffer.getInternalBuffer().toByteArray());
  }

  public void sendBatch(byte[] raw) throws TransportException {
    /*
     * Wrap the call with retry logic to avoid intermittent ES issues.
     */
    Callable<SimpleHttpResponse> callable = () -> {
      SimpleHttpResponse resp;
      String responseString = null;

      SimpleRequestBuilder rb =
          SimpleRequestBuilder.post().setHttpHost(this.target).setPath(this.url.getPath());


      /*
       * Do the call, read response, release connection so it is available for use again, and
       * finally check the response.
       */
      if (this.useGzip) {
        rb = rb.addHeader("Accept-Encoding", "gzip");
        resp = execute(rb, raw, ContentType.DEFAULT_BINARY);
      } else {
        resp = execute(rb, raw, getUncompressedContentType());
      }

      responseString = resp.getBodyText();
      if (responseString == null || responseString == "") {
        responseString = "Empty Reponse";
      }

      checkResponse(resp, responseString);
      return resp;
    };

    RetryConfig config = new RetryConfigBuilder()
        .retryOnSpecificExceptions(TransportException.class).withMaxNumberOfTries(this.retries + 1)
        .withDelayBetweenTries(this.retryDelayMs, ChronoUnit.MILLIS).withExponentialBackoff()
        .build();

    try {
      new CallExecutor(config).execute(callable);
    } catch (RetriesExhaustedException ree) {
      logger.warn("transport failed after " + ree.getCallResults().getTotalTries() + " tries.");
      throw new TransportException(ree.getCallResults().getLastExceptionThatCausedRetry());
    } catch (UnexpectedException ue) {
      throw new TransportException(ue);
    }
  }

  /**
   * Processes the response sent back by HTTP server. Override this method to implement custom
   * response processing logic. Note connection is already released when this method is called.
   * 
   * @param resp Response object from server.
   * @param responseString Response string message from server.
   * @throws TransportException when HTTP call was unsuccessful.
   */
  public void checkResponse(SimpleHttpResponse resp, String responseString)
      throws TransportException {
    /*
     * Check responses status code of the overall bulk call.
     */
    if (resp.getCode() == HttpStatus.SC_OK) {
      return;
    }

    throw new TransportException("http transport call failed because \"" + resp.getReasonPhrase()
        + "\" payload response \"" + responseString + "\"");
  }
}
