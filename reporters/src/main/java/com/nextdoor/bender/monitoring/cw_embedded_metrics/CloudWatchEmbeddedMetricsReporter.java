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

package com.nextdoor.bender.monitoring.cw_embedded_metrics;

import com.amazonaws.services.cloudwatch.model.Dimension;
import com.nextdoor.bender.monitoring.Reporter;
import com.nextdoor.bender.monitoring.Stat;
import com.nextdoor.bender.monitoring.StatFilter;
import com.nextdoor.bender.monitoring.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.nextdoor.bender.monitoring.ReporterUtils.getCloudWatchEmbeddedMetricsJson;
import static com.nextdoor.bender.monitoring.ReporterUtils.tagsToDimensions;

/**
 * This reporter writes metrics by outputting to the Lambda stdout in the CW embedded metrics format
 * Doc link: https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/CloudWatch_Embedded_Metric_Format_Generation.html
 */
public class CloudWatchEmbeddedMetricsReporter implements Reporter {
    private final String namespace;
    private final List<StatFilter> statFilters;

    public CloudWatchEmbeddedMetricsReporter(final String namespace,
                                             final List<StatFilter> statFilters) {
        this.namespace = namespace;
        this.statFilters = statFilters;
    }

    @Override
    public void write(ArrayList<Stat> stats, long invokeTimeMs, Set<Tag> tags) {
        Map<String, String> dimensions = tagsToDimensions(tags).stream()
                .collect(Collectors.toMap(Dimension::getName, Dimension::getValue));

        String metricsJson = getCloudWatchEmbeddedMetricsJson(namespace, invokeTimeMs, dimensions, stats);

        System.out.println(metricsJson);
    }


    @Override
    public List<StatFilter> getStatFilters() {
        return this.statFilters;
    }
}
