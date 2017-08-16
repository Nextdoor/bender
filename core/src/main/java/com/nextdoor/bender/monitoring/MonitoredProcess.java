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

package com.nextdoor.bender.monitoring;

/**
 * Helper class that is used by a processes that need tracking of runtime, errors, and successes.
 */
public class MonitoredProcess {
  private Stat runtimeStat;
  private Stat errorCountStat;
  private Stat successCountStat;

  public MonitoredProcess(Class clazz) {
    String child = clazz.getCanonicalName();
    runtimeStat = new Stat("timing.ns", Stat.MetricType.gauge);
    runtimeStat.addTag("class", child);
    errorCountStat = new Stat("error.count", Stat.MetricType.count);
    errorCountStat.addTag("class", child);
    successCountStat = new Stat("success.count", Stat.MetricType.count);
    successCountStat.addTag("class", child);

    Monitor monitor = Monitor.getInstance();

    monitor.addProcess(this);
    monitor.addInstanceStat(runtimeStat);
    monitor.addInstanceStat(errorCountStat);
    monitor.addInstanceStat(successCountStat);
  }

  public Stat getRuntimeStat() {
    return runtimeStat;
  }

  public void setRuntimeStat(Stat s) {
    this.runtimeStat = s;
  }

  public Stat getErrorCountStat() {
    return errorCountStat;
  }

  public void setErrorCountStat(Stat s) {
    this.errorCountStat = s;
  }

  public Stat getSuccessCountStat() {
    return successCountStat;
  }

  public void setSuccessCountStat(Stat s) {
    this.successCountStat = s;
  }

  public void clearStats() {
    runtimeStat.clear();
    errorCountStat.clear();
    successCountStat.clear();
  }
}
