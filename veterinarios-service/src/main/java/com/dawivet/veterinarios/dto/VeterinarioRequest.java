package com.dawivet.veterinarios.dto;

import lombok.Data;

@Data
public class VeterinarioRequest {
    private String especialidad;
    private String colegiatura;
    private Integer edad;
    private UsuarioDatos usuario;
}