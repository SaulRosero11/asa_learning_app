package com.springboot.asa.learning.domain.exception;

public class UserNotFoundException extends AsaException {
    public UserNotFoundException(String email) {
        super("Usuario no encontrado: " + email);
    }
}
