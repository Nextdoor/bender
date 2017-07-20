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
package com.nextdoor.bender.wrapper.basic;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.wrapper.Wrapper;

/**
 * A basic wrapper that contains a hash of the original event, timestamp of the event, and wrapped
 * event.
 */
public class BasicWrapper implements Wrapper {

  private String sha1Hash;
  private long timestamp;
  private Object payload;

  public BasicWrapper() {

  }

  private BasicWrapper(final InternalEvent internal) {
    this.sha1Hash = internal.getEventSha1Hash();
    this.timestamp = internal.getEventTimeMs();

    if (internal.getEventObj() != null) {
      this.payload = internal.getEventObj().getPayload();
    } else {
      this.payload = null;
    }
  }

  @Override
  public BasicWrapper getWrapped(final InternalEvent internal) {
    return new BasicWrapper(internal);
  }

  public String getSha1Hash() {
    return sha1Hash;
  }

  public Object getPayload() {
    return payload;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
