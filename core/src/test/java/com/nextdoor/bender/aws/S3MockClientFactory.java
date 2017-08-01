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

package com.nextdoor.bender.aws;

import java.net.URI;
import java.util.Properties;

import org.gaul.s3proxy.AuthenticationType;
import org.gaul.s3proxy.S3Proxy;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.junit.rules.TemporaryFolder;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import com.nextdoor.bender.aws.AmazonS3ClientFactory;

public class S3MockClientFactory extends AmazonS3ClientFactory {
  private static final String S3_BUCKET = "testbucket";
  private AmazonS3Client client;
  private S3Proxy s3Proxy;

  public S3MockClientFactory(TemporaryFolder tmpFolder) throws Exception {
    String endpoint = "http://127.0.0.1:8085";
    URI uri = URI.create(endpoint);
    Properties properties = new Properties();
    properties.setProperty("s3proxy.authorization", "none");
    properties.setProperty("s3proxy.endpoint", endpoint);
    properties.setProperty("jclouds.provider", "filesystem");
    properties.setProperty("jclouds.filesystem.basedir", tmpFolder.getRoot().getPath());

    ContextBuilder builder = ContextBuilder.newBuilder("filesystem").credentials("x", "x")
        .modules(ImmutableList.<Module>of(new SLF4JLoggingModule())).overrides(properties);
    BlobStoreContext context = builder.build(BlobStoreContext.class);
    BlobStore blobStore = context.getBlobStore();

    this.s3Proxy = S3Proxy.builder().awsAuthentication(AuthenticationType.NONE, "x", "x")
        .endpoint(uri).keyStore("", "").blobStore(blobStore).build();
    this.s3Proxy.start();

    BasicAWSCredentials awsCredentials = new BasicAWSCredentials("x", "x");

    this.client = new AmazonS3Client(awsCredentials, new ClientConfiguration());
    this.client.setEndpoint(endpoint);
    this.client.createBucket(S3_BUCKET);
  }

  @Override
  public AmazonS3Client newInstance() {
    return this.client;
  }

  public void shutdown() {
    if (this.s3Proxy != null) {
      try {
        this.s3Proxy.stop();
      } catch (Exception e) {
      }
    }
  }
}
