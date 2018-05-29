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
 *
 */

package com.nextdoor.bender.ipc.devnull;

import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.ipc.TransportBuffer;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.ipc.TransportFactory;
import com.nextdoor.bender.ipc.TransportFactoryInitException;
import com.nextdoor.bender.ipc.UnpartitionedTransport;
import com.nextdoor.bender.ipc.generic.GenericTransportBuffer;

import java.io.IOException;

/**
 * Creates a {@link DevNullTransport} from a {@link DevNullTransportConfig}.
 */
public class DevNullTransportFactory implements TransportFactory {
  private DevNullTransportSerializer serializer = new DevNullTransportSerializer();
  private DevNullTransportConfig config;

  @Override
  public UnpartitionedTransport newInstance() throws TransportFactoryInitException {
    return new DevNullTransport();
  }

  @Override
  public Class<DevNullTransport> getChildClass() {
    return DevNullTransport.class;
  }

  @Override
  public void close() {}

  @Override
  public TransportBuffer newTransportBuffer() throws TransportException {
    try {
      return new GenericTransportBuffer(1, false, serializer);
    } catch (IOException e) {
      throw new TransportException(e);
    }
  }

  @Override
  public int getMaxThreads() {
    return 1;
  }

  @Override
  public void setConf(AbstractConfig config) {
    this.config = (DevNullTransportConfig) config;
  }
}
