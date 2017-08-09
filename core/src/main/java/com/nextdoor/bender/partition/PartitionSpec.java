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

package com.nextdoor.bender.partition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.utils.Time;

public class PartitionSpec {
  public enum Interpreter {
    STRING, MILLISECONDS, SECONDS, STATIC
  }

  public enum StringFormat {
    TOLOWER, TOUPPER, NONE
  }

  @JsonSchemaDescription("Value to use as the key for the partition")
  @JsonProperty(required = true)
  private String name;

  @JsonSchemaDescription("Fields to use for the value of the partition")
  private List<String> sources = Collections.emptyList();

  @JsonSchemaDescription("Interpreter to use on the partition value")
  @JsonProperty(required = true)
  @JsonSchemaDefault("STRING")
  private Interpreter interpreter = Interpreter.STRING;

  @JsonSchemaDescription("Java date format to use when using a time based interpreter")
  @JsonProperty(required = false)
  private String format = null;

  @JsonSchemaDescription("Basic string formatting")
  @JsonProperty(required = false)
  @JsonSchemaDefault("NONE")
  private StringFormat stringFormat = StringFormat.NONE;

  private DateTimeFormatter dateTimeFormatter;

  public PartitionSpec() {}

  public PartitionSpec(String name, List<String> sources, Interpreter interpreter, String format) {
    this.name = name;
    this.sources = sources;
    this.interpreter = interpreter;
    this.format = format;
    setDateTimeFormatter();
  }

  public PartitionSpec(String name, List<String> sources, Interpreter interpreter) {
    this.name = name;
    this.sources = sources;
    this.interpreter = interpreter;
  }

  public PartitionSpec(String name, List<String> sources) {
    this.name = name;
    this.sources = sources;
    this.interpreter = Interpreter.STRING;
  }

  public PartitionSpec(String name, String... sources) {
    this.name = name;
    this.sources = Arrays.asList(sources);
    this.interpreter = Interpreter.STRING;
  }

  public String getName() {
    return this.name;
  }

  public List<String> getSources() {
    return this.sources;
  }

  public Interpreter getInterpreter() {
    return this.interpreter;
  }

  public StringFormat getStringFormat() {
    return this.stringFormat;
  }

  public void setStringFormat(StringFormat format) {
    this.stringFormat = format;
  }

  public void setFormat(String format) {
    this.format = format;
    setDateTimeFormatter();
  }

  public String getFormat() {
    return this.format;
  }

  private void setDateTimeFormatter() {
    if (format != null && (this.interpreter == Interpreter.MILLISECONDS
        || this.interpreter == Interpreter.SECONDS)) {
      dateTimeFormatter = DateTimeFormat.forPattern(format).withZoneUTC();
    } else {
      dateTimeFormatter = null;
    }
  }

  public String interpret(String input) {
    switch (this.interpreter) {
      case STRING:
        return getFormattedString(input);
      case STATIC:
        return this.format;
      default:
        return getFormattedTime(input);
    }
  }

  protected String getFormattedString(String input) {
    if (input == null) {
      return input;
    }

    switch (this.stringFormat) {
      case NONE:
        return input;
      case TOUPPER:
        return input.toUpperCase();
      case TOLOWER:
        return input.toLowerCase();
      default:
        return input;
    }
  }

  protected String getFormattedTime(String input) {
    if (input == null || input.equals("")) {
      return null;
    }

    long ts;
    switch (this.interpreter) {
      case SECONDS:
        ts = (long) (Double.parseDouble(input) * 1000);
        break;
      case MILLISECONDS:
        ts = (long) (Double.parseDouble(input));
        break;
      default:
        throw new RuntimeException("Unknown interpreter");
    }

    /*
     * Sanity check
     */
    ts = Time.toMilliseconds(ts);

    return this.dateTimeFormatter.print(ts);
  }

  public String toString() {
    return name + "[" + "sources=" + StringUtils.join(',', sources) + ", interpreter=" + interpreter
        + ", format=" + (format != null ? format : "none") + "]";
  }
}
