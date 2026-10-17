package com.dawivet.vacunas.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(
        boolean success,
        String message,
        ErrorData error,
        String path,
        OffsetDateTime timestamp) {

    public static ErrorResponse of(String message, String code, String path) {
        return of(message, code, List.of(), path);
    }

    public static ErrorResponse of(
            String message, String code, List<FieldErrorDetail> details, String path) {
        return new ErrorResponse(false, message, new ErrorData(code, details), path, OffsetDateTime.now());
    }
}
