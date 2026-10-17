package com.dawivet.vacunas.dto;

import java.time.OffsetDateTime;

public record ApiResponse<T>(boolean success, String message, T data, OffsetDateTime timestamp) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, OffsetDateTime.now());
    }
}
