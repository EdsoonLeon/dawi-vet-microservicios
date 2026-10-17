package com.dawivet.vacunas.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends DomainException {
    public BusinessRuleException(String code, String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, code, message);
    }
}
