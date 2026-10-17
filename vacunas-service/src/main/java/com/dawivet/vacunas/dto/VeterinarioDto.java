package com.dawivet.vacunas.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VeterinarioDto(Integer id, String nombre, String apellido, Boolean activo) {
}
