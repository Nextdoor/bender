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
import java.util.HashSet;
import java.util.Set;

import com.nextdoor.bender.monitoring.Tag;

public class Stat {
  private long value = 0;
  private String name;
  private MetricType type;
  private long startTime;

  private ArrayList<Stat> subStats = new ArrayList<Stat>();
  private Set<Tag> tags = new HashSet<Tag>();

  public enum MetricType {
    count, gauge
  }

  public Stat(String name, long value, MetricType type) {
    this.name = name;
    this.value = value;
    this.type = type;
  }

  public Stat(String name, MetricType type) {
    this.name = name;
    this.type = type;
  }

  public Stat(String name, long value) {
    this.name = name;
    this.value = value;
    this.type = MetricType.count;
  }

  public Stat(String name) {
    this.name = name;
    this.type = MetricType.gauge;
  }

  public void start() {
    this.startTime = System.nanoTime();
  }

  public void stop() {
    value += (System.nanoTime() - this.startTime);
  }

  public long getValue() {
    return value;
  }

  public void setValue(long value) {
    this.value = value;
  }

  public void increment() {
    this.value += 1;
  }

  public String getName() {
    return name;
  }

  public MetricType getType() {
    return type;
  }

  public void addTag(String key, String value) {
    tags.add(new Tag(key, value));
  }

  public void clear() {
    this.startTime = 0;
    this.value = 0;
    this.subStats.clear();
  }

  public Set<Tag> getTags() {
    return tags;
  }

  public synchronized Stat fork() {
    Stat stat = new Stat(this.name, this.type);
    subStats.add(stat);
    return stat;
  }

  /**
   * Aggregates any sub stats that may have been created.
   */
  public void join() {
    for (Stat subStat : subStats) {
      this.value += subStat.getValue();
    }
  }
}
