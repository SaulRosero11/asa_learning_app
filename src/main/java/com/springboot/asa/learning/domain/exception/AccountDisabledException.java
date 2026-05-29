package com.springboot.asa.learning.domain.exception;

public class AccountDisabledException extends AsaException {
    public AccountDisabledException() {
        super("La cuenta está desactivada");
    }
}
