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

package com.nextdoor.bender.monitoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.nextdoor.bender.monitoring.Tag;

public interface Reporter {
  /**
   * @param stats the stats to write.
   * @param invokeTimeMs epoch time in MS when the function was called.
   * @param tags additional tags associated with {@link Stat} events.
   */
  public void write(ArrayList<Stat> stats, long invokeTimeMs, Set<Tag> tags);

  /**
   * @return List of filters provided by configuration.
   */
  public List<StatFilter> getStatFilters();
}
