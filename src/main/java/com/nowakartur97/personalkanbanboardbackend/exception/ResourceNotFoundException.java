package com.nowakartur97.personalkanbanboardbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceType, String fieldType, String value) {
        super(resourceType + " with " + fieldType + ": '" + value + "' not found.");
    }
}
