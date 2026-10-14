package com.dawivet.veterinarios.dto;

import lombok.Data;

@Data
public class UsuarioDatos {
    private String nombre;
    private String apellido;
    private String correo;
    private String clave;
}