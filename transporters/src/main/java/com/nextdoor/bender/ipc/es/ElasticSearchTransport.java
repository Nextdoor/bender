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

package com.nextdoor.bender.ipc.es;

import java.util.HashSet;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.ipc.es.EsResponse.Index;
import com.nextdoor.bender.ipc.es.EsResponse.Item;
import com.nextdoor.bender.ipc.http.HttpTransport;

public class ElasticSearchTransport extends HttpTransport {
  private static final Logger logger = Logger.getLogger(ElasticSearchTransport.class);

  public ElasticSearchTransport(HttpClient client, String url, boolean useGzip, int retries,
      long retryDelayMs) {
    super(client, url, useGzip, retries, retryDelayMs);
  }

  public ElasticSearchTransport(HttpClient client, boolean useGzip) {
    super(client, "/_bulk", useGzip, 0, 1000);
  }

  @Override
  protected ContentType getUncompressedContentType() {
    return ContentType.APPLICATION_JSON;
  }

  @Override
  public void checkResponse(HttpResponse resp, String responseString) throws TransportException {
    /*
     * Check responses status code of the overall bulk call. The call can succeed but have
     * individual failures which are checked later.
     */
    if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
      throw new TransportException(
          "es call failed because " + resp.getStatusLine().getReasonPhrase());
    }

    /*
     * Short circuit deserializing the response by just looking if there are any errors
     */
    if (responseString.contains("\"errors\":false")) {
      return;
    }

    /*
     * Convert response text to a POJO. Only tested with ES 2.4.x but seems to work with 5.x
     */
    Gson gson = new GsonBuilder().create();
    EsResponse esResp = null;
    try {
      esResp = gson.fromJson(responseString, EsResponse.class);
    } catch (JsonSyntaxException e) {
      throw new TransportException(
          "es call failed because " + resp.getStatusLine().getReasonPhrase(), e);
    }

    /*
     * Look for the errors per index request
     */
    int failures = 0;

    if (esResp.items == null) {
      throw new TransportException(
          "es call failed because " + resp.getStatusLine().getReasonPhrase());
    }

    HashSet<String> errorTypes = new HashSet<String>();
    for (Item item : esResp.items) {
      Index index = item.index;

      if (index == null || index.error == null || index.error.reason == null) {
        continue;
      }

      /*
       * For now just allow 200's and 400's. Both are considered non-fatal errors from the lambda's
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
