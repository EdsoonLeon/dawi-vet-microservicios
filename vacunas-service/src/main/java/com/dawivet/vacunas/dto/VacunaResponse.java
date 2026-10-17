package com.dawivet.vacunas.dto;

public record VacunaResponse(
        Integer id,
        String nombre,
        String descripcion,
        String enfermedadPreviene,
        Boolean activo) {
}
