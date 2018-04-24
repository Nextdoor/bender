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

package com.nextdoor.bender.s3proxy;

import java.io.File;
import java.net.URI;
import java.util.Properties;

import org.gaul.s3proxy.AuthenticationType;
import org.gaul.s3proxy.S3Proxy;
import org.gaul.s3proxy.S3Proxy.Builder;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;

import com.google.common.io.Files;


public class Start {
  private static S3Proxy s3Proxy;

  public static void main(String args[]) throws Exception {
    int port = Integer.parseInt(args[0]);
    String username = args[1];
    String password = args[2];

    File storage = Files.createTempDir();
    storage.deleteOnExit();

    URI uri = URI.create("http://127.0.0.1:" + port);
    Properties properties = new Properties();
    properties.setProperty("s3proxy.authorization", "none");
    properties.setProperty("s3proxy.endpoint", "http://127.0.0.1:" + port);
    properties.setProperty("jclouds.provider", "filesystem");
    properties.setProperty("jclouds.filesystem.basedir", storage.getPath());

    Builder s3Builder =
        S3Proxy.builder().awsAuthentication(AuthenticationType.NONE, username, password)
            .endpoint(uri).keyStore("", "");

    ContextBuilder builder =
        ContextBuilder.newBuilder("filesystem").credentials("x", "x").overrides(properties);
    BlobStoreContext context = builder.build(BlobStoreContext.class);
    BlobStore blobStore = context.getBlobStore();

    s3Proxy = s3Builder.blobStore(blobStore).build();
    s3Proxy.start();

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        try {
          if (s3Proxy != null) {
            s3Proxy.stop();
          }
        } catch (Exception e) {
          System.exit(0);
        }
      }
    });
  }
}
