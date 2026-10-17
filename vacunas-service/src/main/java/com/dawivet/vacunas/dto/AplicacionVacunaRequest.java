package com.dawivet.vacunas.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AplicacionVacunaRequest(
        @NotNull(message = "El ID de la vacuna es obligatorio")
        @Positive(message = "El ID de la vacuna debe ser positivo")
        Integer vacunaId,

        @NotNull(message = "El ID de la mascota es obligatorio")
        @Positive(message = "El ID de la mascota debe ser positivo")
        Integer mascotaId,

        @NotNull(message = "El ID del veterinario es obligatorio")
        @Positive(message = "El ID del veterinario debe ser positivo")
        Integer veterinarioId,

        @NotNull(message = "La fecha de aplicación es obligatoria")
        LocalDate fechaAplicacion,

        LocalDate proximaDosis,

        @Size(max = 100, message = "El lote no debe exceder 100 caracteres")
        String lote,

        @Size(max = 500, message = "La observación no debe exceder 500 caracteres")
        String observacion) {
}
