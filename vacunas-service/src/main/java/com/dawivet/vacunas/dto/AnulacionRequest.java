package com.dawivet.vacunas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnulacionRequest(
        @NotBlank(message = "El motivo de anulación es obligatorio")
        @Size(max = 500, message = "El motivo de anulación no debe exceder 500 caracteres")
        String motivo) {
}
