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

package com.nextdoor.bender.filter;

import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.monitoring.MonitoredProcess;

/**
 * Basic interface for filters
 */
public abstract class Filter extends MonitoredProcess {
  public Filter(Class clazz) {
    super(clazz);
  }

  /**
   *
   * @param ievent Internal event with deserialized payload
   * @return True if event matches filter. False if event does not match
   */
  public abstract boolean doFilter(InternalEvent ievent);

  public boolean filter(InternalEvent ievent) {
    this.getRuntimeStat().start();
    try {
      return doFilter(ievent);
    } finally {
      this.getRuntimeStat().stop();
    }
  }
}
