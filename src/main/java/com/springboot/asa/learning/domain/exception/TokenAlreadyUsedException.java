package com.springboot.asa.learning.domain.exception;

public class TokenAlreadyUsedException extends AsaException {
    public TokenAlreadyUsedException() {
        super("Este enlace ya fue utilizado. Solicita uno nuevo.");
    }
}
