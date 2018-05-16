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

package com.nextdoor.bender.ipc.kinesis;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.ipc.TransportFactory;
import com.nextdoor.bender.ipc.UnpartitionedTransport;
import com.nextdoor.bender.ipc.generic.GenericTransportSerializer;

/**
 * Creates a {@link KinesisTransport} from a {@link KinesisTransportConfig}.
 */
public class KinesisTransportFactory implements TransportFactory {
  private KinesisTransportConfig config;
  private AmazonKinesis client;

  @Override
  public UnpartitionedTransport newInstance() {
    return new KinesisTransport(this.client, this.config.getStreamName());
  }

  @Override
  public Class<KinesisTransport> getChildClass() {
    return KinesisTransport.class;
  }

  @Override
  public void close() {}

  @Override
  public KinesisTransportBuffer newTransportBuffer() throws TransportException {
    return new KinesisTransportBuffer(new GenericTransportSerializer(), this.config.getBatchSize());
  }

  @Override
  public int getMaxThreads() {
    return config.getThreads();
  }

  @Override
  public void setConf(AbstractConfig config) {
    this.config = (KinesisTransportConfig) config;
    this.client = AmazonKinesisClientBuilder.standard().withRegion(this.config.getRegion()).build();
  }
}
