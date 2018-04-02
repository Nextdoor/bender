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

package com.nextdoor.bender.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3URI;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.aws.AmazonS3ClientFactory;
import com.nextdoor.bender.config.BenderConfig;
import com.nextdoor.bender.config.ConfigurationException;
import com.nextdoor.bender.config.HandlerResources;
import com.nextdoor.bender.config.Source;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.deserializer.DeserializerProcessor;
import com.nextdoor.bender.ipc.IpcSenderService;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.logging.BenderLayout;
import com.nextdoor.bender.monitoring.Monitor;
import com.nextdoor.bender.monitoring.Stat;
import com.nextdoor.bender.operation.OperationProcessor;
import com.nextdoor.bender.serializer.SerializationException;
import com.nextdoor.bender.serializer.SerializerProcessor;
import com.nextdoor.bender.wrapper.Wrapper;

/**
 * Lambda handler which contains most of the logic to process inputs.
 *
 * @param <T> child handler which implements logic specific to the input
 */
public abstract class BaseHandler<T> implements Handler<T> {
  private static final Logger logger = Logger.getLogger(BaseHandler.class);
  public static String CONFIG_FILE = null;
  protected boolean skipWriteStats = false;
  protected boolean initialized = false;
  protected Wrapper wrapper;
  protected SerializerProcessor ser;
  private IpcSenderService ipcService;
  protected List<Source> sources;
  protected BenderConfig config = null;
  protected Monitor monitor;
  protected AmazonS3ClientFactory s3ClientFactory = new AmazonS3ClientFactory();
  protected HandlerMetadata metadata = null;

  /**
   * Loads @{link com.nextdoor.bender.config.Configuration} from a resource file and initializes
   * classes.
   *
   * @param ctx function context as specified when function is invoked by lambda.
   * @throws HandlerException error while loading the @{link
   *         com.nextdoor.bender.config.Configuration}.
   */
  public void init(Context ctx) throws HandlerException {
    /*
     * Function alias is the last part of the Function ARN
     */
    String alias = null;
    String[] tokens = ctx.getInvokedFunctionArn().split(":");
    if (tokens.length == 7) {
      alias = "$LATEST";
    } else if (tokens.length == 8) {
      alias = tokens[7];
    }
    BenderLayout.ALIAS = alias;
    BenderLayout.VERSION = ctx.getFunctionVersion();

    /*
     * Create a new monitor and then get a static copy of it
     */
    monitor = Monitor.getInstance();
    monitor.addTag("functionName", ctx.getFunctionName());
    monitor.addTag("functionVersion", alias);

    String configFile;

    /*
     * TODO: Replace this to always use env vars. Code was written prior to
     * lambda env vars existing.
     */
    if (System.getenv("BENDER_CONFIG") != null) {
      configFile = System.getenv("BENDER_CONFIG");
    } else if (CONFIG_FILE == null) {
      configFile = "/config/" + alias;
    } else {
      configFile = CONFIG_FILE;
    }

    logger.info(String.format("Bender Initializing (config: %s)", configFile));

    try {
      if (configFile.startsWith("s3://")) {
        config = BenderConfig.load(s3ClientFactory, new AmazonS3URI(configFile));
      } else if (configFile.startsWith("file://")) {
        File file = new File(configFile.replaceFirst("file://", ""));
        String string = FileUtils.readFileToString(file);
        config = BenderConfig.load(configFile, string);
      } else {
        config = BenderConfig.load(configFile);
      }
    } catch (ConfigurationException | IOException e) {
      throw new HandlerException("Error loading configuration: " + e.getMessage(), e);
    }

    HandlerResources handlerResources;
    try {
      handlerResources = new HandlerResources(config);
    } catch (ClassNotFoundException e) {
      throw new HandlerException("Unable to load resource: " + e.getMessage(), e);
    }

    /*
     * Register reporters
     */
    monitor.addReporters(handlerResources.getReporters());

    /*
     * Init other things
     */
    wrapper = handlerResources.getWrapperFactory().newInstance();
    ser = handlerResources.getSerializerProcessor();
    setIpcService(new IpcSenderService(handlerResources.getTransportFactory()));
    sources = new ArrayList<Source>(handlerResources.getSources().values());
    initialized = true;
  }

  /**
   * Wraps entire function in a catch all. This allows for @{link Handler} implementations to do any
   * cleanup before the error is raised and function fails.
   *
   * @param context function context as specified when function is invoked by lambda.
   */
  public void process(Context context) {
    try {
      processInternal(context);
    } catch (Exception e) {
      try {
        this.onException(e);
      } catch (Exception e1) {
        logger.error("Exception thrown in onException handler", e1);
      }

      logger.fatal("Function failure occurred", e);
      if (this.config != null && this.config.getHandlerConfig() != null) {
        if (this.config.getHandlerConfig().getFailOnException()) {
          throw new RuntimeException("function failed", e);
        } else {
          logger.warn("Unrecoverable exception caught");
        }
      } else {
        throw new RuntimeException("function failed", e);
      }
    } finally {
      try {
        this.getInternalEventIterator().close();
      } catch (IOException e) {
        logger.warn("Error closing iterator", e);
      }
    }
  }

  private static void updateOldest(AtomicLong max, long time) {
    while (true) {
      long curMax = max.get();

      /*
       * With time smaller value is older
       */
      if (curMax <= time) {
        return;
      }

      if (max.compareAndSet(curMax, time)) {
        return;
      }
    }
  }

  /**
   * Method called by Handler implementations to process records.
   *
   * @param context Lambda invocation context.
   * @throws HandlerException
   */
  private void processInternal(Context context) throws HandlerException {
    Stat runtime = new Stat("runtime.ns");
    runtime.start();

    Source source = this.getSource();
    DeserializerProcessor deser = source.getDeserProcessor();
    List<OperationProcessor> operations = source.getOperationProcessors();
    List<String> containsStrings = source.getContainsStrings();
    List<Pattern> regexPatterns = source.getRegexPatterns();

    this.getIpcService().setContext(context);

    Iterator<InternalEvent> events = this.getInternalEventIterator();

    /**
     * Prepare our metadata object - this method may be overridden by extensions of the
     * BaseHandler to provide additional Source-specific Metadata.
     */
    prepareMetadata();

    /**
     * Populate the Metadata object with some of the Lambda Context settings
     */
    metadata.setField("functionName", context.getFunctionName());
    metadata.setField("functionVersion", context.getFunctionVersion());
    metadata.setField("processingTime", System.currentTimeMillis());
    metadata.setField("eventSource", this.getSourceName());

    /**
     * Mark the Metadata object as immutable at this point. Future setField operations will fail.
     */
    metadata.setImmutable();

    /*
     * For logging purposes log when the function started running
     */
    this.monitor.invokeTimeNow();

    AtomicLong eventCount = new AtomicLong(0);
    AtomicLong oldestArrivalTime = new AtomicLong(System.currentTimeMillis());
    AtomicLong oldestOccurrenceTime = new AtomicLong(System.currentTimeMillis());

    /*
     * Process each record
     */
    int characteristics = Spliterator.IMMUTABLE;
    Spliterator<InternalEvent> spliterator =
        Spliterators.spliteratorUnknownSize(events, characteristics);
    Stream<InternalEvent> input = StreamSupport.stream(spliterator, false);

    /*
     * Filter out raw events
     */
    Stream<InternalEvent> filtered = input.filter(
        /*
         * Perform regex filter
         */
        ievent -> {
          eventCount.incrementAndGet();
          String eventStr = ievent.getEventString();

          /**
           * Add the HandlerMetadata reference in case any of the future operations need it
           */
          ievent.setMetadata(metadata);

          /*
           * Apply String contains filters before deserialization
           */
          for (String containsString : containsStrings) {
            if (eventStr.contains(containsString)) {
              return false;
            }
          }

          /*
           * Apply regex patterns before deserialization
           */
          for (Pattern regexPattern : regexPatterns) {
            Matcher m = regexPattern.matcher(eventStr);

            if (m.find()) {
              return false;
            }
          }

          return true;
        });


    /*
     * Deserialize
     */
    Stream<InternalEvent> deserialized = filtered.map(ievent -> {
      DeserializedEvent data = deser.deserialize(ievent.getEventString());

      if (data == null || data.getPayload() == null) {
        logger.warn("Failed to deserialize: " + ievent.getEventString());
        return null;
      }

      ievent.setEventObj(data);
      return ievent;
    }).filter(Objects::nonNull);

    /*
     * Perform Operations
     */
    Stream<InternalEvent> operated = deserialized;
    for (OperationProcessor operation : operations) {
      operated = operation.perform(operated);
    }

    /*
     * Serialize
     */
    Stream<InternalEvent> serialized = operated.map(ievent -> {
      try {
        String raw = null;
        raw = this.ser.serialize(this.wrapper.getWrapped(ievent));
        ievent.setSerialized(raw);
        return ievent;
      } catch (SerializationException e) {
        return null;
      }
    }).filter(Objects::nonNull);

    /*
     * Transport
     */
    serialized.forEach(ievent -> {
      /*
       * Update times
       */
      updateOldest(oldestArrivalTime, ievent.getArrivalTime());
      updateOldest(oldestOccurrenceTime, ievent.getEventTime());

      try {
        this.getIpcService().add(ievent);
      } catch (TransportException e) {
        logger.warn("error adding event", e);
      }
    });

    /*
     * Wait for transporters to finish
     */
    try {
      this.getIpcService().shutdown();
    } catch (TransportException e) {
      throw new HandlerException("encounted TransportException while shutting down ipcService", e);
    } catch (InterruptedException e) {
      throw new HandlerException("thread was interruptedwhile shutting down ipcService", e);
    } finally {
      String evtSource = this.getSourceName();

      runtime.stop();

      if (!this.skipWriteStats) {
        writeStats(eventCount.get(), oldestArrivalTime.get(), oldestOccurrenceTime.get(), evtSource,
            runtime);
      }

      if (logger.isTraceEnabled()) {
        getGCStats();
      }
    }
  }

  private void writeStats(long evtCount, long oldestArrivalTime, long oldestOccurrenceTime,
      String source, Stat runtime) {
    /*
     * Add some stats about this invocation
     */
    Stat eventCount = new Stat("event.count", evtCount, Stat.MetricType.count);
    Stat spoutLag = new Stat("spout.lag.ms", (System.currentTimeMillis() - oldestArrivalTime),
        Stat.MetricType.gauge);
    Stat sourceLag = new Stat("source.lag.ms", (System.currentTimeMillis() - oldestOccurrenceTime),
        Stat.MetricType.gauge);

    eventCount.addTag("source", source);
    spoutLag.addTag("source", source);
    sourceLag.addTag("source", source);
    runtime.addTag("source", source);

    this.monitor.addInvocationStat(eventCount);
    this.monitor.addInvocationStat(spoutLag);
    this.monitor.addInvocationStat(sourceLag);
    this.monitor.addInvocationStat(runtime);

    /*
     * Report stats
     */
    this.monitor.writeStats();
  }

  private static long lastGcCount = 0;
  private static long lastGcDuration = 0;

  private void getGCStats() {
    long currentGcCount = 0;
    long currentGcDuration = 0;

    for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
      long count = gc.getCollectionCount();

      if (count >= 0) {
        currentGcCount += count;
      }

      long time = gc.getCollectionTime();

      if (time >= 0) {
        currentGcDuration += time;
      }
    }

    logger.trace("number of GCs: " + (currentGcCount - lastGcCount) + " and time spent in GCs: "
        + (currentGcDuration - lastGcDuration) + "ms");

    lastGcCount = currentGcCount;
    lastGcDuration = currentGcDuration;
  }

  /**
   * Creates a fresh {@Link HandlerMetadata} object for this {@Link BaseHandler} object and
   * returns it back to the caller.
   */
  protected void prepareMetadata() {
    metadata = new HandlerMetadata();
  }

  /**
   * Returns back a reference to the {@Link HandlerMetadata} object. Used by other classes to get
   * data about the function invocation and possibly make mutation or payload decisions based on
   * this data.
   *
   * @return {@Link HandlerMetadata}
   */
  public HandlerMetadata getMetadata() {
    return metadata;
  }

  public IpcSenderService getIpcService() {
    return ipcService;
  }

  public void setIpcService(IpcSenderService ipcService) {
    this.ipcService = ipcService;
  }
}
