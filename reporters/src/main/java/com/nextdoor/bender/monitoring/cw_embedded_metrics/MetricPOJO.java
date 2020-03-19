package com.nextdoor.bender.monitoring.cw_embedded_metrics;

import com.google.gson.annotations.SerializedName;

public class MetricPOJO {
    @SerializedName(value = "Name")
    private String name;

    @SerializedName(value = "Unit")
    private String unit;

    public MetricPOJO(String name, String unit) {
        this.name = name;
        this.unit = unit;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }
}
