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


package com.nextdoor.bender.ipc;

import java.io.IOException;

import com.nextdoor.bender.InternalEvent;

public interface TransportBuffer {
  /**
   * Adds event to the internal buffer.
   *
   * @param ievent event to add to {@link TransportBuffer}.
   * @return True on success and throws IllegalStateException when {@link TransportBuffer} is full.
   * @throws IllegalStateException when buffer is full.
   * @throws IOException error while writing to buffer.
   */
  public boolean add(InternalEvent ievent) throws IllegalStateException, IOException;

  /**
   * Retrieves the internal buffer object.
   *
   * @return Internal buffer object
   */
  public Object getInternalBuffer();

  /**
   * State of buffer.
   *
   * @return True when buffer is empty and false if buffer contains data.
   */
  public boolean isEmpty();

  /**
   * Closes the internal buffer.
   */
  public void close();

  /**
   * Clears the internal buffer.
   */
  public void clear();
}
