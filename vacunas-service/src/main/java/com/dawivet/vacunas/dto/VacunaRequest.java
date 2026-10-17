package com.dawivet.vacunas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VacunaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no debe exceder 120 caracteres")
        String nombre,

        @Size(max = 500, message = "La descripción no debe exceder 500 caracteres")
        String descripcion,

        @NotBlank(message = "La enfermedad que previene es obligatoria")
        @Size(max = 200, message = "La enfermedad que previene no debe exceder 200 caracteres")
        String enfermedadPreviene) {
}
