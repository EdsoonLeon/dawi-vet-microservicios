package com.dawivet.auth.dto;

import static com.dawivet.auth.util.CorreoNormalizador.normalizar;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 255, message = "El correo no puede superar 255 caracteres")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    private String clave;

    public void setCorreo(String correo) {
        this.correo = normalizar(correo);
    }
}
