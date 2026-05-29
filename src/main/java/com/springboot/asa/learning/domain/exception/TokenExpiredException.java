package com.springboot.asa.learning.domain.exception;

public class TokenExpiredException extends AsaException {
    public TokenExpiredException() {
        super("El token ha expirado");
    }
}
