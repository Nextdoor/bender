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

package com.nextdoor.bender.monitoring.cw;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.nextdoor.bender.monitoring.Reporter;
import com.nextdoor.bender.monitoring.Stat;
import com.nextdoor.bender.monitoring.StatFilter;
import com.nextdoor.bender.monitoring.Tag;

/**
 * Writes metrics to Amazon Cloudwatch.
 */
public class CloudwatchReporter implements Reporter {
  private final AmazonCloudWatchClient client = new AmazonCloudWatchClient();
  private final String namespace;
  private final List<StatFilter> statFilters;

  public CloudwatchReporter(final String namespace, final List<StatFilter> statFilters) {
    this.namespace = namespace;
    this.statFilters = statFilters;
  }

  @Override
  public void write(ArrayList<Stat> stats, long invokeTimeMs, Set<Tag> tags) {
    Date dt = new Date();
    dt.setTime(invokeTimeMs);

    Collection<Dimension> parentDims = tagsToDimensions(tags);
    List<MetricDatum> metrics = new ArrayList<MetricDatum>();

    /*
     * Create CW metric objects from bender internal Stat objects
     */
    for (Stat stat : stats) {
      /*
       * Dimension are CW's version of metric tags. A conversion must be done.
       */
      Collection<Dimension> metricDims = tagsToDimensions(stat.getTags());
      metricDims.addAll(parentDims);

      MetricDatum metric = new MetricDatum();
      metric.setMetricName(stat.getName());
      // TODO: add units to Stat object SYSTEMS-870
      metric.setUnit(StandardUnit.None);
      metric.setTimestamp(dt);
      metric.setDimensions(metricDims);
      metric.setValue((double) stat.getValue());

      metrics.add(metric);
    }

    /*
     * Not very well documented in java docs but CW only allows 20 metrics at a time.
     */
    List<List<MetricDatum>> chunks = ListUtils.partition(metrics, 20);
    for (List<MetricDatum> chunk : chunks) {
      PutMetricDataRequest req = new PutMetricDataRequest();
      req.withMetricData(chunk);
      req.setNamespace(namespace);

      this.client.putMetricData(req);
    }
  }

  private Collection<Dimension> tagsToDimensions(final Set<Tag> tags) {
    return tags.stream().map(e -> tagToDim(e.getKey(), e.getValue())).collect(Collectors.toList());
  }

  private Dimension tagToDim(String name, String value) {
    return new Dimension().withName(name).withValue(value != null ? value : "None");
  }

  @Override
  public List<StatFilter> getStatFilters() {
    return this.statFilters;
  }
}
