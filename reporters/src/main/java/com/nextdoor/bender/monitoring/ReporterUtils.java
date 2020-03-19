package com.nextdoor.bender.monitoring;

import com.amazonaws.services.cloudwatch.model.Dimension;

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
}
