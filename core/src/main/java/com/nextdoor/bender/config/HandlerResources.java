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

package com.nextdoor.bender.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.nextdoor.bender.ipc.TransportConfig;
import com.nextdoor.bender.ipc.TransportFactory;
import com.nextdoor.bender.ipc.TransportFactoryFactory;
import com.nextdoor.bender.monitoring.Reporter;
import com.nextdoor.bender.monitoring.ReporterConfig;
import com.nextdoor.bender.monitoring.ReporterFactory;
import com.nextdoor.bender.monitoring.ReporterFactoryFactory;
import com.nextdoor.bender.serializer.SerializerConfig;
import com.nextdoor.bender.serializer.SerializerFactory;
import com.nextdoor.bender.serializer.SerializerFactoryFactory;
import com.nextdoor.bender.serializer.SerializerProcessor;
import com.nextdoor.bender.utils.ReflectionUtils;
import com.nextdoor.bender.wrapper.WrapperConfig;
import com.nextdoor.bender.wrapper.WrapperFactory;

/**
 * HandlerConfig contains all the resources a handler needs to function.
 */
public class HandlerResources {
  private static final Logger logger = Logger.getLogger(HandlerResources.class);
  private Map<String, Source> sources = new HashMap<String, Source>();
  private SerializerProcessor serializerProcessor;
  private WrapperFactory wrapperFactory;
  private TransportFactory transportFactory;
  private List<Reporter> reporters = new ArrayList<Reporter>(0);
  private final TransportFactoryFactory tff = new TransportFactoryFactory();
  private final ReporterFactoryFactory rff = new ReporterFactoryFactory();
  private final SerializerFactoryFactory sff = new SerializerFactoryFactory();

  public HandlerResources(BenderConfig config) throws ClassNotFoundException {
    setSources(config.getSources());
    setSerializerProcessor(config.getSerializerConfig());
    setTransportFactory(config.getTransportConfig());
    setWrapperFactory(config.getWrapperConfig());
    setReporters(config.getReporters());
  }

  public void setSources(List<SourceConfig> sourceConfigs) throws ClassNotFoundException {
    for (SourceConfig sourceConfig : sourceConfigs) {
      Source source = new Source(sourceConfig);
      logger.info("Using source: " + source.toString());
      sources.put(source.getSourceName(), source);
    }
  }

  public Map<String, Source> getSources() {
    return sources;
  }

  public Source getSourceByName(String name) {
    return sources.get(name);
  }

  public void setSerializerProcessor(SerializerConfig serializerConfig)
      throws ClassNotFoundException {
    SerializerFactory sf = sff.getFactory(serializerConfig);
    serializerProcessor = new SerializerProcessor(sf.newInstance());
  }

  public SerializerProcessor getSerializerProcessor() {
    return serializerProcessor;
  }

  public void setWrapperFactory(WrapperConfig config) {
    wrapperFactory = (WrapperFactory) ReflectionUtils.newInstance(config.getFactoryClass());
  }

  public WrapperFactory getWrapperFactory() {
    return wrapperFactory;
  }

  public TransportFactory getTransportFactory() {
    return transportFactory;
  }

  public void setTransportFactory(TransportConfig config) throws ClassNotFoundException {
    this.transportFactory = tff.getFactory(config);
  }

  public void setReporters(List<ReporterConfig> reporterConfigs) throws ClassNotFoundException {
    for (ReporterConfig rconfig : reporterConfigs) {
      ReporterFactory rf = rff.getFactory(rconfig);
      reporters.add((Reporter) rf.newInstance());
    }
  }

  public List<Reporter> getReporters() {
    return reporters;
  }
}
