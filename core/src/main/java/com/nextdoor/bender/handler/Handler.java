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

package com.nextdoor.bender.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.InternalEventIterator;
import com.nextdoor.bender.config.Source;

public interface Handler<T> {
  /**
   * Main entry point to lambda function.
   * 
   * @param event Lambda event that triggered the function.
   * @param context Lambda context that triggered the function.
   * @throws HandlerException thrown when function initialization fails.
   */
  public void handler(T event, Context context) throws HandlerException;

  /**
   * @return source of the event that triggered the function.
   */
  public Source getSource();

  /**
   * @return name of the source that triggerd the function.
   */
  public String getSourceName();

  /**
   * @param e Exception that occurred while function was running. This is unrecoverable and will be
   *        re-thrown after this method runs.
   */
  public void onException(Exception e);

  /**
   * @return iterator that abstracts away where events came from and allows for streaming of event
   *         data.
   */
  public InternalEventIterator<InternalEvent> getInternalEventIterator();
}
