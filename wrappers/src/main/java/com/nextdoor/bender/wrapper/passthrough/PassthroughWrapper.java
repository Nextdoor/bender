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

package com.nextdoor.bender.wrapper.passthrough;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.wrapper.Wrapper;

/**
 * Pseudo wrapper that does not wrap the deserailized payload.
 */
public class PassthroughWrapper implements Wrapper {
  private Object wrapped;

  public PassthroughWrapper() {}

  private PassthroughWrapper(final InternalEvent internal) {
    if (internal == null || internal.getEventObj() == null
        || internal.getEventObj().getPayload() == null) {
      wrapped = null;
    } else {
      wrapped = internal.getEventObj().getPayload();
    }
  }

  public Object getWrapped(final InternalEvent internal) {
    return new PassthroughWrapper(internal).wrapped;
  }
}
