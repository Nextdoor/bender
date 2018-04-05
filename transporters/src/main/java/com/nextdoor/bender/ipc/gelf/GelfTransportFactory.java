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
 * Copyright 2018 Nextdoor.com, Inc
 */

package com.nextdoor.bender.ipc.gelf;

import com.nextdoor.bender.ipc.TransportBuffer;
import com.nextdoor.bender.ipc.generic.GenericTransportSerializer;
import com.nextdoor.bender.ipc.tcp.TcpTransportBuffer;
import com.nextdoor.bender.ipc.tcp.TcpTransportFactory;

/**
 * Creates a Tcp transport that uses the Gelf TCP '\0' message separator
 */
public class GelfTransportFactory extends TcpTransportFactory {

  @Override
  public TransportBuffer newTransportBuffer() {
    return new TcpTransportBuffer(getConfig().getMaxBufferSize(), new GenericTransportSerializer('\0'));
  }

}
