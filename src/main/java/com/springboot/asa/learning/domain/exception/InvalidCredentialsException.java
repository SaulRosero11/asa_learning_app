package com.springboot.asa.learning.domain.exception;

public class InvalidCredentialsException extends AsaException {
    public InvalidCredentialsException() {
        super("Credenciales inválidas");
    }
}
