/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * Copyright 2018 Nextdoor.com, Inc
 */

package com.nextdoor.bender;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.log4j.Logger;

import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.Record;
import com.nextdoor.bender.aws.TestContext;
import com.nextdoor.bender.handler.HandlerException;
import com.nextdoor.bender.handler.kinesis.KinesisHandler;

public class Bender {
  private static final Logger logger = Logger.getLogger(Bender.class);
  private static final String name = System.getProperty("sun.java.command");

  /*
   * Short Handler Names used on the CLI to invoke a particular handler type
   */
  private static final String KINESIS = "kinesishandler";

  /*
   * Defaults for the handler invocations...
   */
  private static final String KINESIS_STREAM_NAME = "log-stream";

  /*
   * Global defaults that are not yet overridable, but one day may be configurable on the CLI.
   */
  private static final String AWS_REGION = "us-east-1";
  private static final String AWS_ACCOUNT = "123456789";

  /**
   * Main entrypoint for the Bender CLI tool - handles the argument parsing and triggers the
   * appropriate methods for ultimately invoking a Bender Handler.
   *
   * @param args
   * @throws ParseException
   */
  public static void main(String[] args) throws ParseException {

    /*
     * Create the various types of options that we support
     */
    Option help = Option.builder("H").longOpt("help").desc("Print this message").build();
    Option handler = Option.builder("h").longOpt("handler").hasArg()
        .desc("Which Event Handler do you want to simulate? \n"
            + "Your options are: KinesisHandler. \n" + "Default: KinesisHandler")
        .build();
    Option source_file = Option.builder("s").longOpt("source_file").required().hasArg()
        .desc("Reference to the file that you want to process. Usage depends "
            + "on the Handler you chose. If you chose KinesisHandler "
            + "then this is a local file.")
        .build();
    Option kinesis_stream_name = Option.builder().longOpt("kinesis_stream_name").hasArg()
        .desc("What stream name should we mimic? " + "Default: " + KINESIS_STREAM_NAME
            + " (Kinesis Handler Only)")
        .build();

    /*
     * Build out the option handler and parse the options
     */
    Options options = new Options();
    options.addOption(help);
    options.addOption(handler);
    options.addOption(kinesis_stream_name);
    options.addOption(source_file);

    /*
     * Prepare our help formatter
     */
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(100);
    formatter.setSyntaxPrefix("usage: BENDER_CONFIG=file://config.yaml java -jar");

    /*
     * Parse the options themselves. Throw an error and help if necessary.
     */
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;

    try {
      cmd = parser.parse(options, args);
    } catch (UnrecognizedOptionException | MissingOptionException | MissingArgumentException e) {
      logger.error(e.getMessage());
      formatter.printHelp(name, options);
      System.exit(1);
    }

    /*
     * The CLI tool doesn't have any configuration files built into it. We require that the user set
     * BENDER_CONFIG to something reasonable.
     */
    if (System.getenv("BENDER_CONFIG") == null) {
      logger.error("You must set the BENDER_CONFIG environment variable. \n"
          + "Valid options include: file://<file>");
      formatter.printHelp(name, options);
      System.exit(1);
    }

    if (cmd.hasOption("help")) {
      formatter.printHelp(name, options);
      System.exit(0);
    }

    /*
     * Depending on the desired Handler, we invoke a specific method and pass in the options (or
     * defaults) required for that handler.
     */
    String handler_value = cmd.getOptionValue(handler.getLongOpt(), KINESIS);
    try {

      switch (handler_value.toLowerCase()) {

        /*
         * Note: We only support Kinesis right now. Will add support for S3/S3SNS down the road.
         */
        case KINESIS:
          invokeKinesisHandler(
              cmd.getOptionValue(kinesis_stream_name.getLongOpt(), KINESIS_STREAM_NAME),
              cmd.getOptionValue(source_file.getLongOpt()));
          break;

        /*
         * Error out if an invalid handler was supplied.
         */
        default:
          logger.error(
              "Invalid Handler Option (" + handler_value + "), valid options are: " + KINESIS);
          formatter.printHelp(name, options);
          System.exit(1);
      }
    } catch (HandlerException e) {
      logger.error("Error executing handler: " + e);
      System.exit(1);
    }
  }

  protected static void invokeKinesisHandler(String stream_name, String source_file)
      throws HandlerException {
    String sourceArn =
        "arn:aws:kinesis:" + AWS_REGION + ":" + AWS_ACCOUNT + ":stream/" + stream_name;
    logger.info("Invoking the Kinesis Handler...");

    TestContext ctx = getContext();
    KinesisHandler handler = new KinesisHandler();

    /*
     * Set the arrival timestamp as the function run time.
     */
    Date approximateArrivalTimestamp = new Date();
    approximateArrivalTimestamp.setTime(System.currentTimeMillis());

    /*
     * Open up the source file for events
     */
    Scanner scan = null;
    try {
      scan = new Scanner(new File(source_file));
    } catch (FileNotFoundException e) {
      logger.error("Could not find source file (" + source_file + "): " + e);
      System.exit(1);
    }

    /*
     * Create a series of KinesisEvents from the source file. All of these events will be treated as
     * a single batch that was pushed to Kinesis, so they will all have the same Arrival Time.
     */
    logger.info("Parsing " + source_file + "...");

    List<KinesisEvent.KinesisEventRecord> events = new ArrayList<KinesisEvent.KinesisEventRecord>();
    int r = 0;

    /*
     * Walk through the source file. For each line in the file, turn the line into a KinesisRecord.
     */
    while (scan.hasNextLine()) {
      String line = scan.nextLine();
      Record rec = new Record();
      rec.withPartitionKey("1").withSequenceNumber(r + "")
          .withData(ByteBuffer.wrap(line.getBytes()))
          .withApproximateArrivalTimestamp(approximateArrivalTimestamp);

      KinesisEventRecord krecord = new KinesisEventRecord();
      krecord.setKinesis(rec);
      krecord.setEventSourceARN(sourceArn);
      krecord.setEventID("shardId-000000000000:" + UUID.randomUUID());
      events.add(krecord);

      r += 1;
    }

    logger.info("Read " + r + " records");

    /*
     * Create the main Kinesis Event object - this holds all of the data and records that will be
     * passed into the Kinesis Handler.
     */
    KinesisEvent kevent = new KinesisEvent();
    kevent.setRecords(events);

    /*
     * Invoke handler
     */
    handler.handler(kevent, ctx);
  }

  /**
   * Generates an Amazon TestContext object that will be used to invoke our Bender Handlers.
   *
   * @return TestContext
   */
  protected static TestContext getContext() {
    TestContext ctx = new TestContext();
    ctx.setFunctionName("cli-main");
    ctx.setInvokedFunctionArn("arn:aws:lambda:" + AWS_REGION + ":" + AWS_ACCOUNT
        + ":function:function_name:function_alias");
    ctx.setAwsRequestId(System.currentTimeMillis() + "");
    return ctx;
  }
}
