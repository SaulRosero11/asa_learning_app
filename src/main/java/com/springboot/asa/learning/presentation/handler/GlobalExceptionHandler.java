package com.springboot.asa.learning.presentation.handler;

import com.springboot.asa.learning.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErrorResponse(int status, String message, String code, Instant timestamp) {}

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage(), "INVALID_CREDENTIALS");
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ErrorResponse> handleAccountDisabled(AccountDisabledException ex) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage(), "ACCOUNT_DISABLED");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), "USER_NOT_FOUND");
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpired(TokenExpiredException ex) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), "TOKEN_EXPIRED");
    }

    @ExceptionHandler(TokenAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handleTokenUsed(TokenAlreadyUsedException ex) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), "TOKEN_ALREADY_USED");
    }

    @ExceptionHandler(DomainNotEligibleException.class)
    public ResponseEntity<ErrorResponse> handleDomainNotEligible(DomainNotEligibleException ex) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), "DOMAIN_NOT_ELIGIBLE");
    }

    @ExceptionHandler(PasswordChangeRequiredException.class)
    public ResponseEntity<ErrorResponse> handlePasswordChangeRequired(PasswordChangeRequiredException ex) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage(), "PASSWORD_CHANGE_REQUIRED");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return error(HttpStatus.BAD_REQUEST, message, "VALIDATION_ERROR");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", "INTERNAL_ERROR");
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, String code) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), message, code, Instant.now()));
    }
}
