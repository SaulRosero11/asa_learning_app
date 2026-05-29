package com.springboot.asa.learning.domain.exception;

public class DomainNotEligibleException extends AsaException {
    public DomainNotEligibleException() {
        super("Este usuario no pertenece al dominio autorizado para roles de liderazgo.");
    }
}
