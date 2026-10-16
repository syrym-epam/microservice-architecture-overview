package com.microservice.resource_service.exceptions;

public class DataReadErrorException extends RuntimeException{

    public DataReadErrorException(String msg) {
        super(msg);
    }
    
}
