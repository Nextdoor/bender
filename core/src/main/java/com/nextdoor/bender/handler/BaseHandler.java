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

package com.nextdoor.bender.handler;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.nextdoor.bender.mutator.MutatorProcessor;
import com.nextdoor.bender.mutator.UnsupportedMutationException;
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

    if (System.getenv("BENDER_CONFIG") != null) {
      configFile = System.getenv("BENDER_CONFIG");
    } else if (CONFIG_FILE == null) {
      configFile = "/config/" + alias + ".json";
    } else {
      configFile = CONFIG_FILE;
    }

    logger.info(String.format("Bender Initializing (config: %s)", configFile));

    try {
      if (configFile.startsWith("s3://")) {
        config = BenderConfig.load(s3ClientFactory, new AmazonS3URI(configFile));
      } else {
        config = BenderConfig.load(getClass().getSuperclass(), configFile);
      }
    } catch (ConfigurationException e) {
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
    List<MutatorProcessor> mutators = source.getMutatorProcessors();
    List<String> containsStrings = source.getContainsStrings();
    List<Pattern> regexPatterns = source.getRegexPatterns();

    Iterator<InternalEvent> events = this.getInternalEventIterator();

    /*
     * For logging purposes log when the function started running
     */
    this.monitor.invokeTimeNow();

    int eventCount = 0;
    long oldestArrivalTime = System.currentTimeMillis();
    long oldestOccurrenceTime = System.currentTimeMillis();
    /*
     * Process each record
     */
    while (events.hasNext()) {
      eventCount++;
      InternalEvent ievent = events.next();
      String eventStr = ievent.getEventString();

      /*
       * Apply String contains filters before deserialization
       */
      boolean filtered = false;
      for (String containsString : containsStrings) {
        if (eventStr.contains(containsString)) {
          filtered = true;
          break;
        }
      }

      /*
       * If match found then skip processing event
       */
      if (filtered) {
        continue;
      }

      /*
       * Apply regex patterns before deserialization
       */
      for (Pattern regexPattern : regexPatterns) {
        Matcher m = regexPattern.matcher(eventStr);

        if (m.find()) {
          filtered = true;
          break;
        }
      }

      /*
       * If match found then skip processing event
       */
      if (filtered) {
        continue;
      }

      /*
       * Process event
       */
      DeserializedEvent data = deser.deserialize(ievent.getEventString());
      ievent.setEventObj(data);
      ievent.setPartitions(deser.getPartitionSpecs());

      if (oldestArrivalTime > ievent.getArrivalTime()) {
        oldestArrivalTime = ievent.getArrivalTime();
      }

      if (oldestOccurrenceTime > ievent.getEventTimeMs()) {
        oldestOccurrenceTime = ievent.getEventTimeMs();
      }

      /*
       * Mutate
       */
      if (mutators.size() > 0 && ievent.getEventObj() != null) {
        try {
          for (MutatorProcessor mutator : mutators) {
            mutator.mutate(ievent.getEventObj());
          }
        } catch (UnsupportedMutationException e) {
          logger.error("Mutation Failed: " + e.toString());
        }
      }

      /*
       * Wrap and Send through serializer
       */
      String serialized = null;
      try {
        serialized = this.ser.serialize(this.wrapper.getWrapped(ievent));
        ievent.setSerialized(serialized);
      } catch (SerializationException e) {
        continue;
      }

      try {
        this.getIpcService().add(ievent);
      } catch (TransportException e) {
        logger.warn("error adding event", e);
      }
    }

    /*
     * This is a blocking call. It waits for all transport threads to finish.
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
        writeStats(eventCount, oldestArrivalTime, oldestOccurrenceTime, evtSource, runtime);
      }
      getGCStats();
    }
  }

  private void writeStats(int evtCount, long oldestArrivalTime, long oldestOccurrenceTime,
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

    logger.info("number of GCs: " + (currentGcCount - lastGcCount) + " and time spent in GCs: "
        + (currentGcDuration - lastGcDuration) + "ms");

    lastGcCount = currentGcCount;
    lastGcDuration = currentGcDuration;
  }

  public IpcSenderService getIpcService() {
    return ipcService;
  }

  public void setIpcService(IpcSenderService ipcService) {
    this.ipcService = ipcService;
  }
}
