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
 * Copyright 2018 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.aws;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.zeroturnaround.exec.InvalidExitValueException;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.nextdoor.bender.aws.AmazonS3ClientFactory;

public class S3MockClientFactory extends AmazonS3ClientFactory {
  private static final String S3_BUCKET = "testbucket";
  private AmazonS3Client client;
  private S3Proxy s3 = new S3Proxy();

  public S3MockClientFactory() {
    this(8085, "x", "x");
  }

  public S3MockClientFactory(int port, String username, String pass) {
    try {
      this.s3.start(port, username, pass);
    } catch (IOException | InterruptedException | InvalidExitValueException | TimeoutException e) {
      throw new RuntimeException(e);
    }

    BasicAWSCredentials awsCredentials = new BasicAWSCredentials(username, pass);
    this.client = new AmazonS3Client(awsCredentials, new ClientConfiguration());
    this.client.setEndpoint("http://127.0.0.1:" + port);
    this.client.createBucket(S3_BUCKET);
  }

  @Override
  public AmazonS3Client newInstance() {
    return this.client;
  }

  public void shutdown() {
    try {
      this.s3.stop();
    } catch (IOException | InterruptedException | TimeoutException e) {
      return;
    }
  }
}
