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

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.ipc.TransportSerializer;

public class ElasticSearchTransportSerializer implements TransportSerializer {

  private final boolean useHashId;
  private final String index;
  private final String type;
  private final DateTimeFormatter dtFormat;

  public ElasticSearchTransportSerializer(boolean useHashId, String type, String index) {
    this(useHashId, type, index, null);
  }

  public ElasticSearchTransportSerializer(boolean useHashId, String type, String index,
      String indexTimeFormat) {
    this.useHashId = useHashId;
    this.index = index;
    this.type = type;

    if (indexTimeFormat != null) {
      this.dtFormat = DateTimeFormat.forPattern(indexTimeFormat).withZoneUTC();
    } else {
      this.dtFormat = null;
    }
  }

  @Override
  public byte[] serialize(InternalEvent ievent) {
    /*
     * Create a JSON line that describes the record for ElasticSearch. This is a bit ugly but
     * preformant.
     *
     * For example: {"index": {"_id": "foo", "_type": "bar", "_index": "baz" }} \n
     *
     */
    StringBuilder payload = new StringBuilder();
    payload.append("{\"index\": {");

    if (this.useHashId) {
      payload.append("\"_id\":");
      payload.append("\"");
      payload.append(ievent.getEventSha1Hash());
      payload.append("\",");
    }

    payload.append("\"_type\":");
    payload.append("\"");
    payload.append(this.type);
    payload.append("\",");

    payload.append("\"_index\":");
    payload.append("\"");
    payload.append(getIndexName(ievent));
    payload.append("\"");

    payload.append("}}\n");

    /*
     * Add the serialized record after the index
     */
    payload.append(ievent.getSerialized());
    payload.append("\n");

    return payload.toString().getBytes();
  }

  private String getIndexName(InternalEvent ievent) {
    if (this.dtFormat == null) {
      return this.index;
    }

    String index = this.index + this.dtFormat.print(ievent.getEventTime());

    return index;
  }
}
