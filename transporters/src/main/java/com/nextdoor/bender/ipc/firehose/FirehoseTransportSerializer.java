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

package com.nextdoor.bender.ipc.firehose;

import java.nio.charset.Charset;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.ipc.TransportSerializer;

public class FirehoseTransportSerializer implements TransportSerializer {
  private Charset charset = Charset.forName("UTF-8");
  private boolean appendNewline = true;
  
  public FirehoseTransportSerializer(boolean appendNewline) {
    this.appendNewline = appendNewline;
  }

  @Override
  public byte[] serialize(InternalEvent ievent) {
    if (this.appendNewline) {
      return (ievent.getSerialized() + '\n').getBytes(this.charset);
    } else {
      return (ievent.getSerialized()).getBytes(this.charset);
    }
  }
}
