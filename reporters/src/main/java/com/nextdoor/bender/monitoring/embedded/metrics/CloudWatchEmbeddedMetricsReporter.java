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

package com.nextdoor.bender.monitoring.embedded.metrics;

import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nextdoor.bender.monitoring.Reporter;
import com.nextdoor.bender.monitoring.Stat;
import com.nextdoor.bender.monitoring.StatFilter;
import com.nextdoor.bender.monitoring.Tag;

import java.util.*;
import java.util.stream.Collectors;

import static com.nextdoor.bender.monitoring.ReporterUtils.tagsToDimensions;

/**
 * This reporter writes metrics by outputting to the Lambda stdout in the CW embedded metrics format
 * Doc link: https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/CloudWatch_Embedded_Metric_Format_Generation.html
 */
public class CloudWatchEmbeddedMetricsReporter implements Reporter {
    private final String namespace;
    private final List<StatFilter> statFilters;
    private final Gson gson;

    public CloudWatchEmbeddedMetricsReporter(final String namespace,
                                             final List<StatFilter> statFilters) {
        this.namespace = namespace;
        this.statFilters = statFilters;
        this.gson = getGson();
    }

    private Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        return gsonBuilder.create();
    }

    @Override
    public void write(ArrayList<Stat> stats, long invokeTimeMs, Set<Tag> tags) {
        Map<String, String> dimensions = tagsToDimensions(tags).stream()
                .collect(Collectors.toMap(Dimension::getName, Dimension::getValue));
        String metricsJson = getCloudWatchEmbeddedMetricsJson(namespace, invokeTimeMs, dimensions, stats, gson);
        System.out.println(metricsJson);
    }


    @Override
    public List<StatFilter> getStatFilters() {
        return this.statFilters;
    }

    /**
     * Example of accepted format:
     * {
     *   "_aws": {
     *     "Timestamp": 1574109732004,
     *     "CloudWatchMetrics": [
     *       {
     *         "Namespace": "lambda-function-metrics",
     *         "Dimensions": [["functionVersion"]],
     *         "Metrics": [
     *           {
     *             "Name": "time",
     *             "Unit": "Milliseconds"
     *           }
     *         ]
     *       }
     *     ]
     *   },
     *   "functionVersion": "$LATEST",
     *   "time": 100,
     *   "requestId": "989ffbf8-9ace-4817-a57c-e4dd734019ee"
     * }
     *
     * Java code example: https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/CloudWatch_Embedded_Metric_Format_Generation_PutLogEvents.html
     */
    public static String getCloudWatchEmbeddedMetricsJson(String namespace,
                                                          long timestamp,
                                                          Map<String, String> dimensions,
                                                          List<Stat> stats,
                                                          Gson gson) {
        Map<String, Object> embeddedMetricsObject = new HashMap<>();

        /* first add nodes for each dimension in the embedded object since it'll be shared by all stats/metrics */
        dimensions.forEach(embeddedMetricsObject::put);

        /* Each stat is essentially a metric so iterate through each stat (metric).
           We can be confident there are now duplicate stats since handler guarantees that
         */
        List<CloudWatchMetricObject> cwMetricObjects = new ArrayList<>();
        stats.forEach(s -> {
            /* each CloudWatchMetric object will have the overall dimensions at the operation level */
            List<String> allDimensions = new ArrayList<>(dimensions.keySet());
            List<Dimension> statDimensions = tagsToDimensions(s.getTags());
            allDimensions.addAll(statDimensions.stream()
                    .map(Dimension::getName)
                    .collect(Collectors.toList()));

            allDimensions = allDimensions.stream()
                    .distinct()
                    .collect(Collectors.toList());

            /* add dimension name/value to main embedded metrics object */
            statDimensions.forEach(d -> embeddedMetricsObject.put(d.getName(), d.getValue()));

            /* add stat metric info as overall field */
            embeddedMetricsObject.put(s.getName(), s.getValue());

            /* each stat will have a unique CloudWatchMetric object */
            cwMetricObjects.add(new CloudWatchMetricObject(namespace,
                    Collections.singletonList(allDimensions),
                    Collections.singletonList(new MetricPOJO(s.getName(), StandardUnit.None.toString())))
            );

        });

        /* finally add overall node with list of CWMetric objects */
        embeddedMetricsObject.put("_aws", new AWSMetricObject(timestamp, cwMetricObjects));

        return gson.toJson(embeddedMetricsObject);
    }

}
