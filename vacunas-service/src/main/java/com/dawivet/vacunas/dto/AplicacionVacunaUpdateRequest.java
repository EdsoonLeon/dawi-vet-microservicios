package com.dawivet.vacunas.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AplicacionVacunaUpdateRequest(
        @NotNull(message = "La fecha de aplicación es obligatoria")
        LocalDate fechaAplicacion,

        LocalDate proximaDosis,

        @Size(max = 100, message = "El lote no debe exceder 100 caracteres")
        String lote,

        @Size(max = 500, message = "La observación no debe exceder 500 caracteres")
        String observacion) {
}
