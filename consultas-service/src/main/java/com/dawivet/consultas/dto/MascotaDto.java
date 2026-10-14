package com.dawivet.consultas.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MascotaDto {
    private Integer id;
    private String nombre;
    private Boolean activo;
}