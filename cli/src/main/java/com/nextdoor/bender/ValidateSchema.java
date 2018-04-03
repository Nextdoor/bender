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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextdoor.bender.config.BenderConfig;
import com.nextdoor.bender.config.BenderConfig.BenderSchema;
import com.nextdoor.bender.config.ConfigurationException;

public class ValidateSchema {

  public static void main(String[] args) throws ParseException, InterruptedException, IOException {

    /*
     * Parse cli arguments
     */
    Options options = new Options();
    options.addOption(Option.builder().longOpt("schema").hasArg()
        .desc("Filename to output schema to. Default: schema.json").build());
    options.addOption(Option.builder().longOpt("configs").hasArgs()
        .desc("List of config files to validate against schema.").build());
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    String schemaFilename = cmd.getOptionValue("schema", "schema.json");
    String[] configFilenames = cmd.getOptionValues("configs");

    /*
     * Validate config files against schema
     */
    BenderSchema schema = new BenderSchema(new File(schemaFilename));
    boolean hasFailures = false;
    for (String configFilename : configFilenames) {
      StringBuilder sb = new StringBuilder();
      Files.lines(Paths.get(configFilename), StandardCharsets.UTF_8).forEach(p -> sb.append(p + "\n"));

      System.out.println("Attempting to validate " + configFilename);
      try {
        ObjectMapper mapper = BenderConfig.getObjectMapper(configFilename);
        BenderConfig.validate(sb.toString(), mapper, schema);
        System.out.println("Valid");
      } catch (ConfigurationException e) {
        System.out.println("Invalid");
        e.printStackTrace();
      }
    }

    if (hasFailures) {
      System.exit(1);
    }
  }
}
