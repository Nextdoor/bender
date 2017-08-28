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

package com.nextdoor.bender.ipc.firehose;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseClient;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.ipc.TransportException;
import com.nextdoor.bender.ipc.TransportFactory;
import com.nextdoor.bender.ipc.UnpartitionedTransport;

/**
 * Creates a {@link FirehoseTransport} from a {@link FirehoseTransportConfig}.
 */
public class FirehoseTransportFactory implements TransportFactory {
  private FirehoseTransportConfig config;
  private FirehoseTransportSerializer serializer;
  private AmazonKinesisFirehoseClient client;

  protected enum FirehoseBuffer {
    BATCH, SIMPLE
  }

  @Override
  public UnpartitionedTransport newInstance() {
    return new FirehoseTransport(this.client, this.config.getStreamName());
  }

  @Override
  public Class<FirehoseTransport> getChildClass() {
    return FirehoseTransport.class;
  }

  @Override
  public void close() {}

  @Override
  public FirehoseTransportBuffer newTransportBuffer() throws TransportException {
    switch (this.config.getFirehoseBuffer()) {
      case SIMPLE:
        return new FirehoseTransportBufferSimple(this.serializer);
      default:
      case BATCH:
        return new FirehoseTransportBufferBatch(this.serializer);
    }
  }

  @Override
  public int getMaxThreads() {
    return config.getThreads();
  }

  @Override
  public void setConf(AbstractConfig config) {
    this.config = (FirehoseTransportConfig) config;
    this.serializer = new FirehoseTransportSerializer(this.config.getAppendNewline());
    this.client = new AmazonKinesisFirehoseClient(new ClientConfiguration().withGzip(true));

    if (this.config.getRegion() != null) {
      this.client.withRegion(this.config.getRegion());
    }
  }
}
