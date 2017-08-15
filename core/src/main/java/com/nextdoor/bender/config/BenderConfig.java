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

package com.nextdoor.bender.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.misc.ErrorBuffer;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import com.nextdoor.bender.aws.AmazonS3ClientFactory;
import com.nextdoor.bender.deserializer.DeserializerConfig;
import com.nextdoor.bender.handler.HandlerConfig;
import com.nextdoor.bender.ipc.TransportConfig;
import com.nextdoor.bender.monitoring.ReporterConfig;
import com.nextdoor.bender.operation.OperationConfig;
import com.nextdoor.bender.serializer.SerializerConfig;
import com.nextdoor.bender.wrapper.WrapperConfig;

@JsonSchemaTitle("Bender - Serverless ETL Framework")
@JsonSchemaDescription("Bender provides an extendable Java framework for creating serverless  ETL functions on AWS Lambda.\n"
    + "It handles the complex plumbing and provides the interfaces necessary to build modules for all aspects of the ETL\n"
    + "process. Check it out at https://github.com/nextdoor/bender."
    + "<br><br><h3>Specifying Configuration</h3><br>" + "<h4>S3 File</h4><br>"
    + "Upload your configuration file to an S3 bucket and when creating your lambda function specify the environment\n"
    + "variable BENDER_CONFIG pointing to your configuration file in S3. For example BENDER_CONFIG=s3://example/bender.json.\n"
    + "Note that your function will require sufficient IAM privileges to read from this file."
    + "<br><br><h4>Embedded File</h4><br>"
    + "If using APEX to deploy your lambda function you can add your configuration file inside a config directory.\n"
    + "See https://github.com/Nextdoor/bender/tree/master/example_project for an example. When using this method name your\n"
    + "configuration files corresponding to your lambda function aliases. Default is $LATEST.json"
    + "<br><br><h3>Variable Substitution</h3><br>"
    + "Your configuration file can contain variables which are substituted for lambda function \n"
    + "environment variables. In your configuration wrap the environment with &lt;&gt; tags. \n"
    + "For example: <br>" + "<pre>{\"foo\": &lt;BAR&gt;}</pre>")
public class BenderConfig {
  private static final Logger logger = Logger.getLogger(BenderConfig.class);
  public static final ConfiguredObjectMapper CMAPPER = new ConfiguredObjectMapper();
  public static final BenderSchema SCHEMA = new BenderSchema("/schema/default.json");

  @JsonSchemaDescription("Handler configuration")
  @JsonProperty(required = false)
  private HandlerConfig handlerConfig;

  @JsonSchemaDescription("Source configurations. This includes deserializer and operators.")
  private List<SourceConfig> sources = Collections.emptyList();

  @JsonSchemaDescription("Wrapper configuration")
  private WrapperConfig wrapperConfig;

  @JsonSchemaDescription("Serializer configuration")
  private SerializerConfig serializerConfig;

  @JsonSchemaDescription("Transport configuration")
  private TransportConfig transportConfig;

  @JsonSchemaDescription("List of reporter configurations")
  private List<ReporterConfig> reporters = Collections.emptyList();

  // Inherited from creation
  private String configFile;

  protected BenderConfig() {

  }

  /**
   * Wrap ObjectMapper in order to provide a configured mapper that can be reused across calls. This
   * is used to speedup unit tests where configurations are frequently being loaded.
   */
  private static class ConfiguredObjectMapper {
    private ObjectMapper objectMapper = new ObjectMapper();

    public ConfiguredObjectMapper() {
      objectMapper.registerSubtypes(AbstractConfig.getSubtypes(HandlerConfig.class));
      objectMapper.registerSubtypes(AbstractConfig.getSubtypes(DeserializerConfig.class));
      objectMapper.registerSubtypes(AbstractConfig.getSubtypes(OperationConfig.class));
      objectMapper.registerSubtypes(AbstractConfig.getSubtypes(WrapperConfig.class));
      objectMapper.registerSubtypes(AbstractConfig.getSubtypes(SerializerConfig.class));
      objectMapper.registerSubtypes(AbstractConfig.getSubtypes(TransportConfig.class));
      objectMapper.registerSubtypes(AbstractConfig.getSubtypes(ReporterConfig.class));
    }

    public ObjectMapper getObjectMapper() {
      return this.objectMapper.copy();
    }
  }

  /**
   * Wrap JsonNode containing JSON schema for Bender in order to provide a schema object. This is
   * used to speedup unit tests where configurations are frequently being loaded.
   */
  public static class BenderSchema {
    private JsonNode schema;
    private String schemaFile;

    public BenderSchema() {
      this.schema = genSchema();
      return;
    }

    public BenderSchema(String schemaFile) {
      this.schemaFile = schemaFile;
    }

    public BenderSchema(File schemaFile) {
      try {
        this.schema = CMAPPER.getObjectMapper().readTree(schemaFile);
      } catch (IOException e) {
        throw new ConfigurationException("unable to load schema file", e);
      }
    }


    private JsonNode genSchema() {
      ObjectMapper objectMapper = CMAPPER.getObjectMapper();
      objectMapper.setPropertyNamingStrategy(
          PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
      JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(objectMapper);
      return jsonSchemaGenerator.generateJsonSchema(BenderConfig.class);
    }

    public JsonNode getSchema() {
      /*
       * Lazy load
       */
      if (this.schema == null && this.schemaFile != null) {
        String json;
        try {
          json = IOUtils
              .toString(new InputStreamReader(getClass().getResourceAsStream(schemaFile), "UTF-8"));
        } catch (NullPointerException | IOException e) {
          logger.warn("Unable to find schema file. Auto generating schema.");
          this.schema = genSchema();
          return this.schema;
        }

        try {
          this.schema = CMAPPER.getObjectMapper().readTree(json);
          return this.schema;
        } catch (Exception e) {
          throw new ConfigurationException("unable generate schema");
        }
      }

      return this.schema;
    }
  }

  public static boolean validate(Class clazz, String resource) throws ConfigurationException {
    String json;
    try {
      json = IOUtils.toString(new InputStreamReader(clazz.getResourceAsStream(resource), "UTF-8"));
    } catch (NullPointerException | IOException e) {
      throw new ConfigurationException("unable to find " + resource);
    }

    return validate(json);
  }

  public static boolean validate(String json, String schemaFilename) throws ConfigurationException {
    BenderSchema schema = new BenderSchema(new File(schemaFilename));
    return validate(json, schema);
  }

  public static boolean validate(String json) throws ConfigurationException {
    return validate(json, SCHEMA);
  }

  public static boolean validate(String json, BenderSchema benderSchema)
      throws ConfigurationException {
    ProcessingReport report;
    try {
      /*
       * Create object
       */
      ObjectMapper objectMapper = CMAPPER.getObjectMapper();
      JsonNode node = objectMapper.readTree(json);

      /*
       * Create JSON schema
       */
      JsonNode jsonSchema = benderSchema.getSchema();

      /*
       * Validate
       */
      final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
      final JsonSchema schema = factory.getJsonSchema(jsonSchema);
      report = schema.validate(node);
    } catch (IOException | ProcessingException ioe) {
      throw new ConfigurationException("unable to validate config", ioe);
    }

    if (report.isSuccess()) {
      return true;
    } else {
      throw new ConfigurationException("invalid config file",
          report.iterator().next().asException());
    }
  }

  /**
   * Parses an input String and replaces instances of {@literal <XXX>}" with the value of the XXX OS
   * Environment Variable. This is used as a pre-parser for the Config files, allowing environment
   * variables to be swapped at run-time.
   *
   * @param raw A raw string (not necessarily valid configuration data)
   * @return A parsed string with OS variables swapped in
   * @throws ConfigurationException If any discovered {@literal <WRAPPED_VALUES>} are not found in
   *         System.getenv().
   */
  public static String swapEnvironmentVariables(String raw) throws ConfigurationException {
    ErrorBuffer errors = new ErrorBuffer();
    ST template = new ST(raw);
    STGroup g = template.groupThatCreatedThisInstance;
    g.setListener(errors);

    Map<String, String> env = System.getenv();
    for (String envName : env.keySet()) {
      template.add(envName, env.get(envName));
    }

    String parsed = template.render();

    if (errors.errors.size() > 0) {
      throw new ConfigurationException(errors.toString());
    }

    return parsed;
  }

  public static BenderConfig load(String json) {
    json = swapEnvironmentVariables(json);
    validate(json);

    /*
     * Configure Mapper and register polymorphic types
     */
    ObjectMapper mapper = CMAPPER.getObjectMapper();
    mapper.setPropertyNamingStrategy(
        PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

    BenderConfig config = null;
    try {
      config = mapper.readValue(json, BenderConfig.class);
    } catch (IOException e) {
      throw new ConfigurationException("invalid config file", e);
    }

    return config;
  }

  public static BenderConfig load(AmazonS3ClientFactory s3ClientFactory, AmazonS3URI s3Uri) {
    AmazonS3Client s3 = s3ClientFactory.newInstance();
    S3Object s3object = s3.getObject(s3Uri.getBucket(), s3Uri.getKey());

    StringWriter writer = new StringWriter();

    try {
      IOUtils.copy(s3object.getObjectContent(), writer, "UTF-8");
    } catch (IOException e) {
      throw new ConfigurationException("Unable to read file from s3", e);
    }

    BenderConfig config = load(writer.toString());
    config.setConfigFile(s3Uri.getURI().toString());

    return config;
  }

  public static BenderConfig load(Class clazz, String resource) {
    /*
     * Read config file
     */
    String json;
    try {
      json = IOUtils.toString(new InputStreamReader(clazz.getResourceAsStream(resource), "UTF-8"));
    } catch (NullPointerException | IOException e) {
      throw new ConfigurationException("unable to find " + resource);
    }

    BenderConfig config = load(json);
    config.setConfigFile(resource);

    return config;
  }

  @JsonProperty("handler")
  public HandlerConfig getHandlerConfig() {
    return this.handlerConfig;
  }

  @JsonProperty("handler")
  public void setHandlerConfig(HandlerConfig handlerConfig) {
    this.handlerConfig = handlerConfig;
  }

  @JsonProperty("transport")
  public TransportConfig getTransportConfig() {
    return this.transportConfig;
  }

  @JsonProperty("transport")
  public void setTransportConfig(TransportConfig transport) {
    this.transportConfig = transport;
  }

  @JsonProperty("wrapper")
  public WrapperConfig getWrapperConfig() {
    return this.wrapperConfig;
  }

  @JsonProperty("wrapper")
  public void setWrapperConfig(WrapperConfig wrapperConfig) {
    this.wrapperConfig = wrapperConfig;
  }

  @JsonProperty("serializer")
  public SerializerConfig getSerializerConfig() {
    return this.serializerConfig;
  }

  @JsonProperty("serializer")
  public void setSerializerConfig(SerializerConfig serializerConfig) {
    this.serializerConfig = serializerConfig;
  }

  @JsonProperty("reporters")
  public List<ReporterConfig> getReporters() {
    return reporters;
  }

  @JsonProperty("reporters")
  public void setReporters(List<ReporterConfig> reporters) {
    this.reporters = reporters;
  }

  @JsonProperty("sources")
  public List<SourceConfig> getSources() {
    return sources;
  }

  @JsonProperty("sources")
  public void setSources(List<SourceConfig> sources) {
    this.sources = sources;
  }

  @JsonIgnore
  public String getConfigFile() {
    return configFile;
  }

  @JsonIgnore
  public void setConfigFile(String configFile) {
    this.configFile = configFile;
  }
}
