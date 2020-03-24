package com.nextdoor.bender.operation.delete;

import com.nextdoor.bender.config.AbstractConfig;
import com.nextdoor.bender.operation.OperationFactory;

public class DeleteFieldOperationFactory implements OperationFactory {
    private DeleteFieldOperationConfig config;

    @Override
    public void setConf(AbstractConfig config) {
        this.config = (DeleteFieldOperationConfig) config;
    }

    @Override
    public Class<DeleteFieldOperation> getChildClass() {
        return DeleteFieldOperation.class;
    }

    @Override
    public DeleteFieldOperation newInstance() {
        return new DeleteFieldOperation(config.getKeyField());
    }
}
