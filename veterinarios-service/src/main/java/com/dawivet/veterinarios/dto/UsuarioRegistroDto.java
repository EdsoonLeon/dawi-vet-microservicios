package com.dawivet.veterinarios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRegistroDto {
    private String nombre;
    private String apellido;
    private String correo;
    private String clave;
    private RolDto rol;
}