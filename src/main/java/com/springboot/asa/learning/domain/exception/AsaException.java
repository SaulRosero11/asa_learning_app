package com.springboot.asa.learning.domain.exception;

public abstract class AsaException extends RuntimeException {
    public AsaException(String message) {
        super(message);
    }
}
