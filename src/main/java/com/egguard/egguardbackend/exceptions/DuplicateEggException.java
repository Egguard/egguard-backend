package com.egguard.egguardbackend.exceptions;

public class DuplicateEggException extends RuntimeException {
    public DuplicateEggException(String message) {
        super(message);
    }
} 