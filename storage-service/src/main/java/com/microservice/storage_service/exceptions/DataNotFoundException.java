package com.microservice.storage_service.exceptions;



public class DataNotFoundException extends RuntimeException{

    public DataNotFoundException(String s) {
        super(s);
    }
    
}
