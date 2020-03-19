package com.nextdoor.bender.monitoring.cw_embedded_metrics;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class CloudWatchMetricObject {
    @SerializedName(value = "Namespace")
    private String namespace;

    @SerializedName(value = "Dimensions")
    private List<List<String>> dimensions = new ArrayList<>();

    @SerializedName(value = "Metrics")
    private List<MetricPOJO> metrics = new ArrayList<>();

    public CloudWatchMetricObject(String namespace,
                                  List<List<String>> dimensions,
                                  List<MetricPOJO> metrics) {
        this.namespace = namespace;
        this.dimensions = dimensions;
        this.metrics = metrics;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setDimensions(List<List<String>> dimensions) {
        this.dimensions = dimensions;
    }

    public List<List<String>> getDimensions() {
        return dimensions;
    }

    public void setMetrics(List<MetricPOJO> metrics) {
        this.metrics = metrics;
    }

    public List<MetricPOJO> getMetrics() {
        return metrics;
    }
}
