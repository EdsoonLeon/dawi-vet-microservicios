package com.dawivet.auth.dto;

import static com.dawivet.auth.util.CorreoNormalizador.normalizar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioRegistroRequest(
        @Size(max = 255, message = "El nombre no puede superar 255 caracteres")
        String nombre,
        @Size(max = 255, message = "El apellido no puede superar 255 caracteres")
        String apellido,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 255, message = "El correo no puede superar 255 caracteres")
        String correo,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String clave,
        @NotNull(message = "El rol es obligatorio")
        @Valid
        RolReferenciaRequest rol) {

    public UsuarioRegistroRequest {
        correo = normalizar(correo);
    }
}
