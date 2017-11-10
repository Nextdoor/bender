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

package com.nextdoor.bender.operations.geo;

import java.io.IOException;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.nextdoor.bender.aws.AmazonS3ClientFactory;
import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.operation.OperationFactory;

/**
 * Create a {@link GeoIpOperation}.
 */
public class GeoIpOperationFactory implements OperationFactory {
  protected AmazonS3ClientFactory s3Factory = new AmazonS3ClientFactory();
  private DatabaseReader databaseReader;

  private GeoIpOperationConfig config;

  @Override
  public GeoIpOperation newInstance() {
    return new GeoIpOperation(this.config.getIpAddrField(), this.config.getDestinationFieldName(),
        this.databaseReader, this.config.getGeoProperties(), this.config.getFailOnNotFound());
  }

  @Override
  public Class<GeoIpOperation> getChildClass() {
    return GeoIpOperation.class;
  }

  @Override
  public void setConf(AbstractConfig config) {
    this.config = (GeoIpOperationConfig) config;
    AmazonS3Client client = this.s3Factory.newInstance();

    AmazonS3URI uri = new AmazonS3URI(this.config.getGeoLiteDb());
    GetObjectRequest req = new GetObjectRequest(uri.getBucket(), uri.getKey());
    S3Object obj = client.getObject(req);

    try {
      this.databaseReader =
          new DatabaseReader.Builder(obj.getObjectContent()).withCache(new CHMCache()).build();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
