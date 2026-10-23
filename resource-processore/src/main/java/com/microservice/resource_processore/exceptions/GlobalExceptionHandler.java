package com.microservice.resource_processore.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.microservice.resource_processore.dto.ErrorResponseDTO;


@RestControllerAdvice 
public class GlobalExceptionHandler {

    @ExceptionHandler(MetaDataFieldResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> metaDataResourceNotFoundException(MetaDataFieldResourceNotFoundException ex) {

        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(
            "" + HttpStatus.BAD_REQUEST.value(),
            "Cannot find metadata field '%s'".formatted(ex.getMessage())
        );


        return new ResponseEntity<>(errorResponseDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler()
    public ResponseEntity<ErrorResponseDTO> restTemplateRequestError(RestTemplateErrorException ex) {

        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(
            "" + HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Error operation with another external service.\nError message: '%s'".formatted(ex.getMessage())
        );

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    } 
}