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

package com.nextdoor.bender.ipc.datadog;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.config.value.ValueConfig;
import com.nextdoor.bender.ipc.TransportSerializer;

public class DatadogTransportSerializer implements TransportSerializer {

  private final ValueConfig<?> apiKey;

  DatadogTransportSerializer(ValueConfig<?> apiKey) {
    this.apiKey = apiKey;
  }

  @Override
  public byte[] serialize(InternalEvent event) {
    StringBuilder payload = new StringBuilder();
    payload.append(apiKey);
    payload.append(' ');
    payload.append(event.getSerialized());
    payload.append('\n');
    return payload.toString().getBytes();
  }

}
