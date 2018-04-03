/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * Copyright 2018 Nextdoor.com, Inc
 */

package com.nextdoor.bender.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

/**
 * BenderLayout formats log messages in JSON format. This allows for faster and easier filtering of
 * CW logs using the JSON filters. See:
 *
 * http://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/FilterAndPatternSyntax.html
 *
 * Notably this layout contains version and alias information of the running instance of the lambda
 * function.
 */
public class BenderLayout extends Layout {
  private static final Gson GSON =
      new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
          .disableHtmlEscaping().create();
  private static final DateTimeFormatter FORMATTER = ISODateTimeFormat.dateTime().withZoneUTC();
  public static String ALIAS;
  public static String VERSION;

  private static class ExceptionLog {
    public List<String> stacktrace = new ArrayList<String>();
    public String message;
    @SerializedName("class")
    public String clazz;
  }

  private static class BenderLogEntry {
    public ExceptionLog exception;
    public String threadName;
    public long posixTimestamp;
    public String timestamp;
    public String logger;
    public String level;
    public int lineNumber;
    public String method;
    @SerializedName("class")
    public String clazz;
    public String file;
    public String message;
    public String version;
    public String alias;
  }

  @Override
  public void activateOptions() {

  }

  @Override
  public String format(LoggingEvent event) {
    BenderLogEntry entry = new BenderLogEntry();
    entry.threadName = event.getThreadName();
    entry.posixTimestamp = event.getTimeStamp();
    entry.timestamp = FORMATTER.print(entry.posixTimestamp);
    entry.message = event.getRenderedMessage();
    entry.level = event.getLevel().toString();
    entry.logger = event.getLogger().getName();
    entry.alias = ALIAS;
    entry.version = VERSION;

    if (event.getThrowableInformation() != null) {
      final ThrowableInformation throwableInfo = event.getThrowableInformation();
      ExceptionLog ex = new ExceptionLog();

      if (throwableInfo.getThrowable().getClass().getCanonicalName() != null) {
        ex.clazz = throwableInfo.getThrowable().getClass().getCanonicalName();
      }
      if (throwableInfo.getThrowable().getMessage() != null) {
        ex.message = throwableInfo.getThrowable().getMessage();
      }
      if (throwableInfo.getThrowableStrRep() != null) {
        Arrays.asList(throwableInfo.getThrowableStrRep()).forEach(m -> {
          ex.stacktrace.add(m.replaceAll("\\t", "   "));
        });
      }
      entry.exception = ex;
    }

    LocationInfo locinfo = event.getLocationInformation();
    entry.file = locinfo.getFileName();
    entry.lineNumber = Integer.parseInt(locinfo.getLineNumber());
    entry.method = locinfo.getMethodName();
    entry.clazz = locinfo.getClassName();

    return GSON.toJson(entry) + "\n";
  }

  @Override
  public boolean ignoresThrowable() {
    return false;
  }
}
