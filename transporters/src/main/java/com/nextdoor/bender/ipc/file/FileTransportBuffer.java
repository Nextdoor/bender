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

package com.nextdoor.bender.ipc.file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.ipc.TransportBuffer;

public class FileTransportBuffer implements TransportBuffer {
  private static final Logger logger = Logger.getLogger(FileTransportBuffer.class);
  private ByteArrayOutputStream baos = new ByteArrayOutputStream();
  private long recordCounter = 0;
  private FileTransportSerializer serializer;

  public FileTransportBuffer(FileTransportSerializer serializer) {
    this.serializer = serializer;
  }

  @Override
  public boolean add(InternalEvent ievent) throws IllegalStateException, IOException {
    this.baos.write(serializer.serialize(ievent));
    this.recordCounter++;
    return true;
  }

  @Override
  public ByteArrayOutputStream getInternalBuffer() {
    return this.baos;
  }

  @Override
  public boolean isEmpty() {
    return this.recordCounter == 0;
  }

  @Override
  public void close() {
    try {
      this.baos.flush();
    } catch (IOException e) {
      logger.warn("unable to flush baos");
    }
    try {
      this.baos.close();
    } catch (IOException e) {
      logger.warn("unable to close baos");
    }
  }

  @Override
  public void clear() {
    this.baos.reset();
  }
}
