package com.springboot.asa.learning.domain.exception;

public class PasswordChangeRequiredException extends AsaException {
    public PasswordChangeRequiredException() {
        super("Debes cambiar tu contraseña antes de continuar.");
    }
}
