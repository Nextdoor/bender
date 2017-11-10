package com.nextdoor.bender.operation.json.key;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.operation.OperationConfig;

@JsonTypeName("JsonReplaceKVOperation")
@JsonSchemaDescription("Replaces a given key with specified value.")
public class JsonReplaceKVOperationConfig extends OperationConfig {

     @JsonSchemaDescription("Key name to find")
     @JsonProperty(required = true)
     private String key;

     @JsonSchemaDescription("New string value")
     @JsonProperty(required = true)
     private String value;

     @Override
     public Class<JsonReplaceKVOperationFactory> getFactoryClass() {
    return JsonReplaceKVOperationFactory.class;
     }

     
     public String getKey() {
       return this.key;
     }
     
     public void setKey(String key) {
       this.key = key;
     }
     
     public String getValue() {
       return this.value;
     }
     
     public void setValue(String value) {
       this.value = value;
     }
}


