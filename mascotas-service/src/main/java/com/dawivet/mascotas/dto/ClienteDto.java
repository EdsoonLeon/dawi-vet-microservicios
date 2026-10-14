package com.dawivet.mascotas.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClienteDto {
    private Integer id;
    private String nombre;
    private String apellido;
    private Boolean activo;
}