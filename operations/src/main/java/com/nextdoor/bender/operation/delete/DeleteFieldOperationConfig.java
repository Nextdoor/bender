package com.nextdoor.bender.operation.delete;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.nextdoor.bender.operation.OperationConfig;

@JsonTypeName("DeleteFieldOperation")
@JsonSchemaDescription("Deletes the inputted field in the deserialized object. " +
        "When using JSON use JsonPath format to specify field. See https://github.com/jayway/JsonPath")
public class DeleteFieldOperationConfig extends OperationConfig {

    @JsonSchemaDescription("Name of field key to be deleted.")
    @JsonProperty(required = true)
    private String keyField;

    @Override
    public Class<DeleteFieldOperationFactory> getFactoryClass() {
        return DeleteFieldOperationFactory.class;
    }

    public String getKeyField() {
        return keyField;
    }

    public void setKeyField(String keyField) {
        this.keyField = keyField;
    }
}
