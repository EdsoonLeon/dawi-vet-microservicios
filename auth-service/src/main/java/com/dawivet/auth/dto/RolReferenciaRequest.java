package com.dawivet.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RolReferenciaRequest(
        @NotNull(message = "El rol es obligatorio")
        @Positive(message = "El identificador del rol debe ser positivo")
        Integer id) {
}
