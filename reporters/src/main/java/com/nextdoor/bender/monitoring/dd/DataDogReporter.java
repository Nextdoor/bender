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

package com.nextdoor.bender.monitoring.dd;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.nextdoor.bender.monitoring.Reporter;
import com.nextdoor.bender.monitoring.Stat;
import com.nextdoor.bender.monitoring.StatFilter;
import com.nextdoor.bender.monitoring.Tag;

/**
 * Writes to lambda stdout which is parsed by DataDog. See
 * http://docs.datadoghq.com/integrations/awslambda/ for integration setup.
 */
public class DataDogReporter implements Reporter {
  private final String prefix;
  private final List<StatFilter> statFilters;

  public DataDogReporter(final String prefix, final List<StatFilter> statFilters) {
    this.prefix = prefix;
    this.statFilters = statFilters;
  }

  @Override
  public void write(ArrayList<Stat> stats, long invokeTimeMs, Set<Tag> tags) {
    Set<Tag> allTags = new HashSet<Tag>();

    /*
     * DataDog only tracks to second precision
     */
    long ts = invokeTimeMs / 1000;

    for (Stat stat : stats) {
      allTags.addAll(tags);
      allTags.addAll(stat.getTags());

      String tagsString = allTags.stream().map(e -> formatEntry(e.getKey(), e.getValue()))
          .collect(Collectors.joining(","));

      /*
       * MONITORING|unix_epoch_timestamp|value|metric_type|my.metric.name|#tag1:value,tag2
       */
      String[] logParts = {"MONITORING", "" + ts, "" + stat.getValue(), stat.getType().name(),
          prefix + "." + stat.getName(), "#" + tagsString};

      allTags.clear();
      System.out.println(String.join("|", logParts));
    }
  }

  private static String formatEntry(String key, String value) {
    return key + ":" + value;
  }

  @Override
  public List<StatFilter> getStatFilters() {
    return this.statFilters;
  }
}
