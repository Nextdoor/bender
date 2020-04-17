package com.nextdoor.bender.handler;

public class KinesisIteratorException extends RuntimeException {

    public KinesisIteratorException(Exception e) {
        super("Unable to use the iterator", e);
    }

    public KinesisIteratorException(String msg, Exception e) {
        super(msg, e);
    }
}
