package com.dawivet.vacunas.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MascotaDto(Integer id, String nombre, Boolean activo) {
}
