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

package com.nextdoor.bender.ipc.file;

import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.ipc.TransportBuffer;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.ipc.TransportFactory;
import com.nextdoor.bender.ipc.TransportFactoryInitException;
import com.nextdoor.bender.ipc.UnpartitionedTransport;

/**
 * Creates a {@link FileTransport} from a {@link FileTransportConfig}.
 */
public class FileTransportFactory implements TransportFactory {
  private FileTransportSerializer serializer = new FileTransportSerializer();
  private FileTransportConfig config;

  @Override
  public UnpartitionedTransport newInstance() throws TransportFactoryInitException {
    return new FileTransport(config.getFilename());
  }

  @Override
  public Class<FileTransport> getChildClass() {
    return FileTransport.class;
  }

  @Override
  public void close() {}

  @Override
  public TransportBuffer newTransportBuffer() throws TransportException {
    return new FileTransportBuffer(serializer);
  }

  @Override
  public int getMaxThreads() {
    return 1;
  }

  @Override
  public void setConf(AbstractConfig config) {
    this.config = (FileTransportConfig) config;
  }
}
