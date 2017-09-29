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

package com.nextdoor.bender.ipc.scalyr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.evanlennick.retry4j.CallExecutor;
import com.evanlennick.retry4j.RetryConfig;
import com.evanlennick.retry4j.RetryConfigBuilder;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import com.nextdoor.bender.ipc.TransportBuffer;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.ipc.UnpartitionedTransport;

/**
 * Transporter that uses the Scalyr HTTP API
 *
 * https://www.scalyr.com/help/api-uploadLogs
 *
 */
public class ScalyrTransport implements UnpartitionedTransport {
  private final HttpClient client;
  private final boolean useGzip;
  private final long retryDelayMs;
  private final int retries;
  private final String url;

  private static final Logger logger = Logger.getLogger(ScalyrTransport.class);

  protected ScalyrTransport(HttpClient client, String url, boolean useGzip,
      int retries, long retryDelayMs) {
    this.client = client;
    this.url = url;
    this.useGzip = useGzip;
    this.retries = retries;
    this.retryDelayMs = retryDelayMs;
  }

  protected HttpResponse sendBatchUncompressed(byte[] raw) throws TransportException {
    HttpEntity entity = new ByteArrayEntity(raw, ContentType.DEFAULT_TEXT);

    final HttpPost httpPost = new HttpPost(this.url);
    httpPost.setEntity(entity);

    /*
     * Make call
     */
    HttpResponse resp = null;
    try {
      resp = client.execute(httpPost);
    } catch (IOException e) {
      throw new TransportException("failed to make call", e);
    } finally {
      httpPost.releaseConnection();
    }

    return resp;
  }

  protected HttpResponse sendBatchCompressed(byte[] raw) throws TransportException {
    /*
     * Write gzip data to Entity and set content encoding to gzip
     */
    HttpEntity entity = new ByteArrayEntity(raw, ContentType.DEFAULT_BINARY);
    ((ByteArrayEntity) entity).setContentEncoding("gzip");

    final HttpPost httpPost = new HttpPost(this.url);
    httpPost.addHeader(new BasicHeader("Accept-Encoding", "gzip"));
    httpPost.setEntity(entity);

    /*
     * Make call
     */
    HttpResponse resp = null;
    try {
      resp = client.execute(httpPost);
    } catch (IOException e) {
      throw new TransportException("failed to make call", e);
    } finally {
      httpPost.releaseConnection();
    }

    return resp;
  }

  @Override
  public void sendBatch(TransportBuffer buf) throws TransportException {
    com.nextdoor.bender.ipc.scalyr.ScalyrTransportBuffer buffer = (com.nextdoor.bender.ipc.scalyr.ScalyrTransportBuffer) buf;
    sendBatch(buffer.getInternalBuffer().toByteArray());
  }

  protected void sendBatch(byte[] raw) throws TransportException {
    /*
     * Wrap the call with retry logic to avoid intermittent issues.
     */
    Callable<HttpResponse> callable = () -> {
      HttpResponse resp;
      if (this.useGzip) {
        resp = sendBatchCompressed(raw);
      } else {
        resp = sendBatchUncompressed(raw);
      }

      checkResponse(resp);
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
   * Reads a HttpEntity containing gzip content and outputs a String.
   *
   * @param ent entity to read.
   * @return payload contained by the entity.
   * @throws UnsupportedOperationException if getContent failed.
   * @throws IOException reading entity payload failed.
   */
  private String readCompressedResponse(HttpEntity ent)
      throws UnsupportedOperationException, IOException {
    GZIPInputStream gzip = new GZIPInputStream(ent.getContent());;
    BufferedReader br = new BufferedReader(new InputStreamReader(gzip));
    StringBuilder sb = new StringBuilder();

    String line;
    while ((line = br.readLine()) != null) {
      sb.append(line);
    }

    return sb.toString();
  }

  protected void checkResponse(HttpResponse resp) throws TransportException {
    /*
     * Check responses status code of the overall bulk call.
     */
    if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
      return;
    }

    /*
     * Read the http response to a String. If compression was used in the request the response is
     * also compressed and must be decompressed.
     */
    HttpEntity ent = resp.getEntity();

    String responseString;
    try {
      if (this.useGzip) {
        responseString = readCompressedResponse(ent);
      } else {
        responseString = EntityUtils.toString(ent);
      }
    } catch (ParseException | IOException e) {
      throw new TransportException(
          "scalyr call failed because " + resp.getStatusLine().getReasonPhrase());
    }

    throw new TransportException("scalyr call failed because " + responseString);
  }
}
