package com.nextdoor.bender.monitoring.embedded.metrics;

import com.google.gson.Gson;
import com.nextdoor.bender.monitoring.Stat;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStreamReader;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class CloudWatchEmbeddedMetricsReporterTest {
    private Long timestamp = 1584580665491L;
    private Gson gson = new Gson();

    @Test
    public void testWithNoStats() throws Exception {
        String actualJson = CloudWatchEmbeddedMetricsReporter.getCloudWatchEmbeddedMetricsJson("nextdoor",
                timestamp,
                Collections.emptyMap(),
                Collections.emptyList(),
                gson);

        String expectedJson = IOUtils.toString(
                new InputStreamReader(this.getClass().getResourceAsStream("empty_metrics.json"), "UTF-8"));

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testWithNoDimensionsAndStats() throws Exception {
        List<Stat> stats = Arrays.asList(new Stat("counts", 20),
                new Stat("errors", 2));
        String actualJson = CloudWatchEmbeddedMetricsReporter.getCloudWatchEmbeddedMetricsJson("nextdoor",
                timestamp,
                Collections.emptyMap(),
                stats,
                gson);

        String expectedJson = IOUtils.toString(
                new InputStreamReader(this.getClass().getResourceAsStream("metrics_with_stats_no_dimensions.json"), "UTF-8"));

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testWithDimensionsOnSomeStats() throws Exception {
        Stat s1 = new Stat("counts", 20);
        s1.addTag("stage", "prod");
        s1.addTag("region", "us-east-1");
        Stat s2 = new Stat("errors", 2);
        List<Stat> stats = Arrays.asList(s1, s2);

        String actualJson = CloudWatchEmbeddedMetricsReporter.getCloudWatchEmbeddedMetricsJson("nextdoor",
                timestamp,
                Collections.emptyMap(),
                stats,
                gson);

        String expectedJson = IOUtils.toString(
                new InputStreamReader(this.getClass().getResourceAsStream("metrics_with_stats_varying_dimensions.json"), "UTF-8"));

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testWithOverallAndGranularDimensions() throws Exception {
        Stat s1 = new Stat("counts", 20);
        s1.addTag("stage", "prod");
        s1.addTag("region", "us-east-1");
        Stat s2 = new Stat("errors", 2);
        List<Stat> stats = Arrays.asList(s1, s2);

        Map<String, String> dimensions = new HashMap<>();
        dimensions.put("team", "systems");
        dimensions.put("company", "nextdoor");

        String actualJson = CloudWatchEmbeddedMetricsReporter.getCloudWatchEmbeddedMetricsJson("nextdoor",
                timestamp,
                dimensions,
                stats,
                gson);

        String expectedJson = IOUtils.toString(
                new InputStreamReader(this.getClass().getResourceAsStream("metrics_with_stats_many_dimensions.json"), "UTF-8"));

        assertEquals(expectedJson, actualJson);
    }

}
