package com.dawivet.vacunas.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.dawivet.vacunas.model.EstadoAplicacion;

public record AplicacionVacunaResponse(
        Integer id,
        Integer vacunaId,
        Integer mascotaId,
        Integer veterinarioId,
        LocalDate fechaAplicacion,
        LocalDate proximaDosis,
        String lote,
        String observacion,
        String mascotaNombre,
        String veterinarioNombre,
        String vacunaNombre,
        EstadoAplicacion estado,
        String motivoAnulacion,
        LocalDateTime fechaAnulacion) {
}
