package com.dawivet.vacunas.exception;

import org.springframework.http.HttpStatus;

public class ExternalServiceException extends DomainException {
    public ExternalServiceException(HttpStatus status, String code, String message) {
        super(status, code, message);
    }
}
