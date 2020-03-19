package com.nextdoor.bender.monitoring.cw_embedded_metrics;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AWSMetricObject {
    @SerializedName(value = "Timestamp")
    private long timestamp;

    @SerializedName(value = "CloudWatchMetrics")
    private List<CloudWatchMetricObject> cloudWatchMetricObjects;

    public AWSMetricObject(long timestamp,
                           List<CloudWatchMetricObject> cloudWatchMetricObjects) {
        this.timestamp = timestamp;
        this.cloudWatchMetricObjects = cloudWatchMetricObjects;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public List<CloudWatchMetricObject> getCloudWatchMetricObjects() {
        return cloudWatchMetricObjects;
    }

    public void setCloudWatchMetricObjects(List<CloudWatchMetricObject> cloudWatchMetricObjects) {
        this.cloudWatchMetricObjects = cloudWatchMetricObjects;
    }
}
