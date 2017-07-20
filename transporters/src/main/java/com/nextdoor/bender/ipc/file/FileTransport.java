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
 * Copyright 2016 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.ipc.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.nextdoor.bender.ipc.TransportBuffer;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.ipc.UnpartitionedTransport;

/**
 * Writes events to a local file. This is only for testing purposes.
 */
public class FileTransport implements UnpartitionedTransport {
  public String filename;

  public FileTransport(String filename) {
    this.filename = filename;
  }

  @Override
  public void sendBatch(TransportBuffer buffer) throws TransportException {
    FileTransportBuffer buf = (FileTransportBuffer) buffer;

    FileOutputStream out = null;
    try {
      out = new FileOutputStream(new File(this.filename));
      out.write(buf.getInternalBuffer().toByteArray());
    } catch (IOException e) {
      throw new TransportException("unable to write to file", e);
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {

        }
      }
    }
  }
}
