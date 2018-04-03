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

package com.nextdoor.bender.ipc.splunk;

import java.util.Locale;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.ipc.TransportSerializer;

public class SplunkTransportSerializer implements TransportSerializer {
  private final String index;
  
  public SplunkTransportSerializer(String index) {
    this.index = index;
  }

  @Override
  public byte[] serialize(InternalEvent ievent) {
    /*
     * Create a JSON line that describes the record for Splunk.
     */
    StringBuilder payload = new StringBuilder();
    payload.append("{");
    if (this.index != null) {
      payload.append("\"index\":\""+ this.index +"\",");
    }
    payload.append("\"event\":");
    payload.append(ievent.getSerialized());
    payload.append(",\"time\": ");
    payload.append(String.format(Locale.US, "%.3f", ievent.getEventTime()/1000.0));
    payload.append("}");

    return payload.toString().getBytes();
  }
}
