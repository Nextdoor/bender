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

package com.nextdoor.bender.testutils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.monitoring.Reporter;
import com.nextdoor.bender.monitoring.ReporterConfig;
import com.nextdoor.bender.monitoring.ReporterFactory;
import com.nextdoor.bender.monitoring.Stat;
import com.nextdoor.bender.monitoring.StatFilter;
import com.nextdoor.bender.monitoring.Tag;

public class DummyReporterHelper {
  public static class DummyReporter implements Reporter {
    private List<StatFilter> statFilters;
    public List<String> buffer = new ArrayList<>();

    public DummyReporter(List<StatFilter> statFilters) {
      this.statFilters = statFilters;
    }

    @Override
    public void write(ArrayList<Stat> stats, long invokeTimeMs, Set<Tag> tags) {
      Set<Tag> allTags = new HashSet<>();

      for (Stat stat : stats) {
        allTags.addAll(tags);
        allTags.addAll(stat.getTags());
        String tagsString = allTags.stream().map(e -> formatEntry(e.getKey(), e.getValue()))
            .collect(Collectors.joining(","));

        String metric = String.format("%s %s %d", stat.getName(), tagsString, stat.getValue());
        buffer.add(metric);
        allTags.clear();
      }
    }

    @Override
    public List<StatFilter> getStatFilters() {
      return this.statFilters;
    }

    private static String formatEntry(String key, String value) {
      return key + ":" + value;
    }
  }

  public static class DummyReporterFactory implements ReporterFactory {
    private DummyReporterConifg config;

    @Override
    public Reporter newInstance() {
      return new DummyReporter(this.config.getStatFilters());
    }

    @Override
    public void setConf(AbstractConfig config) {
      this.config = (DummyReporterConifg) config;
    }

    @Override
    public Class<DummyReporter> getChildClass() {
      return DummyReporter.class;
    }
  }

  @JsonTypeName("DummyReporter$DummyReporterConfig")
  public static class DummyReporterConifg extends ReporterConfig {

    @Override
    public Class<DummyReporterFactory> getFactoryClass() {
      return DummyReporterFactory.class;
    }
  }
}
