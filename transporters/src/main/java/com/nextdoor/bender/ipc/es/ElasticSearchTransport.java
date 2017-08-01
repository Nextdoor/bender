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
 * Copyright 2016 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.ipc.es;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import com.evanlennick.retry4j.CallExecutor;
import com.evanlennick.retry4j.RetryConfig;
import com.evanlennick.retry4j.RetryConfigBuilder;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nextdoor.bender.ipc.TransportBuffer;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.ipc.UnpartitionedTransport;
import com.nextdoor.bender.ipc.es.EsResponse.Index;
import com.nextdoor.bender.ipc.es.EsResponse.Item;

/**
 * Transporter that uses the ES bulk index http api. Note this has only been tested against ES 2.4.x
 * bulk api responses.
 */
public class ElasticSearchTransport implements UnpartitionedTransport {
  private final RestClient client;
  private final boolean useGzip;
  private final long retryDelayMs;
  private final int retries;

  private static final Logger logger = Logger.getLogger(ElasticSearchTransport.class);

  protected ElasticSearchTransport(RestClient client, boolean useGzip, int retries,
      long retryDelayMs) {
    this.client = client;
    this.useGzip = useGzip;
    this.retries = retries;
    this.retryDelayMs = retryDelayMs;
  }

  protected ElasticSearchTransport(RestClient client, boolean useGzip) {
    this(client, useGzip, 1, 0);
  }

  protected Response sendBatchUncompressed(byte[] raw) throws TransportException {
    HttpEntity entity = new ByteArrayEntity(raw, ContentType.APPLICATION_JSON);

    /*
     * Make call
     */
    try {
      return client.performRequest("POST", "/_bulk", Collections.<String, String>emptyMap(),
          entity);
    } catch (IOException e) {
      throw new TransportException("failed to make call", e);
    }
  }

  protected Response sendBatchCompressed(byte[] raw) throws TransportException {
    /*
     * Write gzip data to Entity and set content encoding to gzip
     */
    HttpEntity entity = new ByteArrayEntity(raw, ContentType.DEFAULT_BINARY);
    ((ByteArrayEntity) entity).setContentEncoding("gzip");

    /*
     * Make call
     */
    try {
      return client.performRequest("POST", "/_bulk", Collections.<String, String>emptyMap(), entity,
          new BasicHeader("Accept-Encoding", "gzip"));
    } catch (IOException e) {
      throw new TransportException("failed to make call", e);
    }
  }

  @Override
  public void sendBatch(TransportBuffer buf) throws TransportException {
    ElasticSearchTransportBuffer buffer = (ElasticSearchTransportBuffer) buf;
    sendBatch(buffer.getInternalBuffer().toByteArray());
  }

  protected void sendBatch(byte[] raw) throws TransportException {
    /*
     * Wrap the call with retry logic to avoid intermittent ES issues.
     */
    Callable<Response> callable = () -> {
      Response resp;
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
      logger.debug("transport failed after " + ree.getCallResults().getTotalTries() + " tries.");
      throw new TransportException(ree.getCallResults().getLastExceptionThatCausedRetry());
    } catch (UnexpectedException ue) {
      throw new TransportException(ue);
    } finally {
      try {
        client.close();
      } catch (IOException e) {
        logger.warn("error occurred while closing http client", e);
      }
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

  /**
   * Deserializes the response from ES and checks for per index request errors. Currently only
   * tested with ES 2.4.x responses.
   *
   * @param resp response from ES.
   * @throws TransportException unable to parse response or response has index failures.
   */
  protected void checkResponse(Response resp) throws TransportException {
    /*
     * Check responses status code of the overall bulk call. The call can succeed but have
     * individual failures which are checked later.
     */
    if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
      throw new TransportException(
          "es call failed because " + resp.getStatusLine().getReasonPhrase());
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
      throw new TransportException("unable to read es response", e);
    }

    /*
     * Convert response text to a POJO
     */
    Gson gson = new GsonBuilder().create();
    EsResponse esResp = gson.fromJson(responseString, EsResponse.class);

    /*
     * EsResponse provides an easy way to check if there are any errors. If there aren't we can exit
     * early.
     */
    if (!esResp.errors) {
      return;
    }

    /*
     * Look for the errors per index request
     */
    int failures = 0;
    HashSet<String> errorTypes = new HashSet<String>();
    for (Item item : esResp.items) {
      Index index = item.index;

      if (index == null) {
        continue;
      }

      if (index.error != null && index.error.reason != null
          && index.error.reason.startsWith("blocked")) {
        continue;
      }

      /*
       * For now just handle 200's and 400's. Both are considered non-fatal errors from the lambda's
       * perspective.
       */
      switch (index.status) {
        case HttpStatus.SC_OK:
        case HttpStatus.SC_BAD_REQUEST:
          continue;
        default:
          failures++;

          if (index.error != null && index.error.reason != null && index.error.type != null) {
            if (!errorTypes.contains(index.error.type)) {
              logger.error("Indexing Error Reason: " + index.error.reason);
              if (index.error.caused_by != null) {
                logger.error("Indexing Error Cause: " + index.error.caused_by.reason);
              }
              errorTypes.add(index.error.type);
            }
          }
      }
    }

    errorTypes.clear();
    if (failures != 0) {
      throw new TransportException("es index failure count is " + failures);
    }
  }
}
