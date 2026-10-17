package com.dawivet.vacunas.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends DomainException {
    public BadRequestException(String code, String message) {
        super(HttpStatus.BAD_REQUEST, code, message);
    }
}
