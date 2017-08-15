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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nextdoor.bender.config.BenderConfig.BenderSchema;

public class CreateSchema {

  public static void main(String[] args) throws ParseException, InterruptedException, IOException {

    /*
     * Parse cli arguments
     */
    Options options = new Options();
    options.addOption(Option.builder().longOpt("out-file").hasArg()
        .desc("Filename to output schema to. Default: schema.json").build());
    options.addOption(Option.builder().longOpt("docson").hasArg(false)
        .desc("Create a schema that is able to be read by docson").build());
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    String filename = cmd.getOptionValue("out-file", "schema.json");

    /*
     * Write schema
     */
    BenderSchema schema = new BenderSchema();
    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);

    JsonNode node = schema.getSchema();

    if (cmd.hasOption("docson")) {
      modifyNode(node);
    }

    mapper.writeValue(new File(filename), node);
  }

  private static void modifyNode(JsonNode schema) {
    List<JsonNode> parents = schema.findParents("items");

    for (JsonNode parent : parents) {
      if (parent.hasNonNull("type") && parent.get("type").asText().equals("array")) {
        if (parent.get("items").hasNonNull("oneOf")) {
          ((ObjectNode) parent).put("anyOf", parent.get("items").get("oneOf"));
          ((ObjectNode) parent).remove("items");
        }
      }
    }
  }
}
