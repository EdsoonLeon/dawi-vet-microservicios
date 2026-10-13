package com.dawivet.auth.dto;

import com.dawivet.auth.model.Usuario;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Usuario usuario;
}