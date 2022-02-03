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

package com.nextdoor.bender;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.event.S3EventNotification.S3BucketEntity;
import com.amazonaws.services.s3.event.S3EventNotification.S3Entity;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.event.S3EventNotification.S3ObjectEntity;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.sns.AmazonSNSClient;

public class S3SnsNotifier {
  private static final Logger logger = Logger.getLogger(S3SnsNotifier.class);
  private static DateTimeFormatter formatter;
  private static HashSet<String> alreadyPublished = new HashSet<>();
  private static boolean dryRun;

  public static void main(String[] args) throws ParseException, InterruptedException, IOException {
    formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZoneUTC();

    /*
     * Parse cli arguments
     */
    Options options = new Options();
    options.addOption(Option.builder().longOpt("bucket").hasArg().required()
        .desc("Name of S3 bucket to list s3 objects from").build());
    options.addOption(Option.builder().longOpt("key-file").hasArg().required()
        .desc("Local file of S3 keys to process").build());
    options.addOption(Option.builder().longOpt("sns-arn").hasArg().required()
        .desc("SNS arn to publish to").build());
    options.addOption(Option.builder().longOpt("throttle-ms").hasArg()
        .desc("Amount of ms to wait between publishing to SNS").build());
    options.addOption(Option.builder().longOpt("processed-file").hasArg()
        .desc("Local file to use to store procssed S3 object names").build());
    options.addOption(Option.builder().longOpt("skip-processed").hasArg(false)
        .desc("Whether to skip S3 objects that have been processed").build());
    options.addOption(Option.builder().longOpt("dry-run").hasArg(false)
        .desc("If set do not publish to SNS").build());

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    String bucket = cmd.getOptionValue("bucket");
    String keyFile = cmd.getOptionValue("key-file");
    String snsArn = cmd.getOptionValue("sns-arn");
    String processedFile = cmd.getOptionValue("processed-file", null);
    boolean skipProcessed = cmd.hasOption("skip-processed");
    dryRun = cmd.hasOption("dry-run");
    long throttle = Long.parseLong(cmd.getOptionValue("throttle-ms", "-1"));

    if (processedFile != null) {
      File file = new File(processedFile);

      if (!file.exists()) {
        logger.debug("creating local file to store processed s3 object names: " + processedFile);
        file.createNewFile();
      }
    }

    /*
     * Import S3 keys that have been processed
     */
    if (skipProcessed && processedFile != null) {
      try (BufferedReader br = new BufferedReader(new FileReader(processedFile))) {
        String line;
        while ((line = br.readLine()) != null) {
          alreadyPublished.add(line.trim());
        }
      }
    }

    /*
     * Setup writer for file containing processed S3 keys
     */
    FileWriter fw = null;
    BufferedWriter bw = null;
    if (processedFile != null) {
      fw = new FileWriter(processedFile, true);
      bw = new BufferedWriter(fw);
    }

    /*
     * Create clients
     */
    AmazonS3Client s3Client = new AmazonS3Client();
    AmazonSNSClient snsClient = new AmazonSNSClient();

    /*
     * Get S3 object list
     */
    try (BufferedReader br = new BufferedReader(new FileReader(keyFile))) {
      String line;
      while ((line = br.readLine()) != null) {
        String key = line.trim();

        if (alreadyPublished.contains(key)) {
          logger.info("skipping " + key);
        }

        ObjectMetadata om = s3Client.getObjectMetadata(bucket, key);

        S3EventNotification s3Notification = getS3Notification(key, bucket, om.getContentLength());

        String json = s3Notification.toJson();

        /*
         * Publish to SNS
         */
        if (publish(snsArn, json, snsClient, key) && processedFile != null) {
          bw.write(key + "\n");
          bw.flush();
        }

        if (throttle != -1) {
          Thread.sleep(throttle);
        }

      }
    }

    if (processedFile != null) {
      bw.close();
      fw.close();
    }
  }

  public static boolean publish(String arn, String msg, AmazonSNSClient snsClient, String s3Key) {
    if (dryRun) {
      logger.warn("would have published " + s3Key + " S3 creation event to SNS");
      return true;
    }

    logger.info("publishing " + s3Key + " S3 creation event to SNS");

    try {
      snsClient.publish(arn, msg, "Amazon S3 Notification");
    } catch (RuntimeException e) {
      logger.error("error publishing", e);
      return false;
    }

    return true;
  }

  public static S3EventNotification getS3Notification(String key, String bucket, long size) {
    S3ObjectEntity objEntity = new S3ObjectEntity(key, size, null, null);
    S3BucketEntity bucketEntity = new S3BucketEntity(bucket, null, null);
    S3Entity entity = new S3Entity(null, bucketEntity, objEntity, null);

    String timestamp = formatter.print(System.currentTimeMillis());
    S3EventNotificationRecord rec =
        new S3EventNotificationRecord(null, null, null, timestamp, null, null, null, entity, null);

    List<S3EventNotificationRecord> notifications = new ArrayList<>(1);
    notifications.add(rec);

    return new S3EventNotification(notifications);
  }
}
