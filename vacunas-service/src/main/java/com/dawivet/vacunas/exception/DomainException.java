package com.dawivet.vacunas.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class DomainException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public DomainException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
