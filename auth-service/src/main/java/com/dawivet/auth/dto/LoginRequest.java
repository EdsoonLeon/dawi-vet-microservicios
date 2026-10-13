package com.dawivet.auth.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String correo;
    private String clave;
}