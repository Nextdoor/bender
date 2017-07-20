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

import com.nextdoor.bender.config.ConfigurableFactory;

/**
 * Helps construct a new {@link Transport} as well as {@link TransportBuffer}.
 */
public interface TransportFactory extends ConfigurableFactory {
  Transport newInstance() throws TransportFactoryInitException;

  TransportBuffer newTransportBuffer() throws TransportException;

  /**
   * Cleans up any internal state the factory may have created while creating new Transport or
   * TransportBuffer instances.
   */
  void close();

  /**
   * Number of threads the {@link IpcSenderService} will use in a thread pool to throttle sends.
   * 
   * @return number of threads to use.
   */
  int getMaxThreads();
}
