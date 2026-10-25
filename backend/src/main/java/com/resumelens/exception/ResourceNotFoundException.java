package com.resumelens.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException() {
        super("The requested resource was not found.");
    }

    public ResourceNotFoundException(String resource, Object id) {
        super("The requested resource was not found.");
    }
}