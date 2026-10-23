package com.microservice.resource_processore.exceptions;

public class RestTemplateErrorException extends RuntimeException {

    public RestTemplateErrorException(String msg) {
        super(msg);
    }
}
