/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright 2017 Nextdoor.com, Inc
 */

package com.nextdoor.bender.ipc.scalyr;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.ipc.TransportSerializer;

public class ScalyrTransportSerializer implements TransportSerializer {
  public ScalyrTransportSerializer() {}

  @Override
  public byte[] serialize(InternalEvent ievent) {
    /*
     * Create a JSON line that describes the record for Scalyr.
     */
    StringBuilder payload = new StringBuilder();
    payload.append(ievent.getSerialized());
    payload.append("\n");

    return payload.toString().getBytes();
  }
}
