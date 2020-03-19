package com.nextdoor.bender.monitoring;

import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nextdoor.bender.monitoring.cw_embedded_metrics.AWSMetricObject;
import com.nextdoor.bender.monitoring.cw_embedded_metrics.CloudWatchMetricObject;
import com.nextdoor.bender.monitoring.cw_embedded_metrics.MetricPOJO;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utils class to capture static methods used across multiple reporters
 */
public class ReporterUtils {
    public static List<Dimension> tagsToDimensions(final Set<Tag> tags) {
        return tags.stream().map(e -> tagToDim(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    private static Dimension tagToDim(String name, String value) {
        return new Dimension().withName(name).withValue(value != null ? value : "None");
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
                                                          List<Stat> stats) {

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();

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
