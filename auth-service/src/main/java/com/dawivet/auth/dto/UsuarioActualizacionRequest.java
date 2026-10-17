package com.dawivet.auth.dto;

import static com.dawivet.auth.util.CorreoNormalizador.normalizar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioActualizacionRequest(
        @Size(max = 255, message = "El nombre no puede superar 255 caracteres")
        String nombre,
        @Size(max = 255, message = "El apellido no puede superar 255 caracteres")
        String apellido,
        @Email(message = "El correo no tiene un formato válido")
        @Pattern(regexp = ".*\\S.*", message = "El correo no puede estar vacío")
        @Size(max = 255, message = "El correo no puede superar 255 caracteres")
        String correo,
        String clave,
        @Valid
        RolReferenciaRequest rol) {

    public UsuarioActualizacionRequest {
        correo = normalizar(correo);
    }
}
