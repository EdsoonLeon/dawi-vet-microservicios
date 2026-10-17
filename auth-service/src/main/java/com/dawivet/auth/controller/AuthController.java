package com.dawivet.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dawivet.auth.dto.LoginRequest;
import com.dawivet.auth.dto.LoginResponse;
import com.dawivet.auth.dto.UsuarioRegistroRequest;
import com.dawivet.auth.model.Usuario;
import com.dawivet.auth.security.JwtUtil;
import com.dawivet.auth.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService service;
    private final JwtUtil jwtUtil;

    @PostMapping("/registrar")
    public ResponseEntity<Usuario> registrar(@Valid @RequestBody UsuarioRegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarUsuario(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = service.login(request.getCorreo(), request.getClave());
        return ResponseEntity.ok(new LoginResponse(jwtUtil.generarToken(usuario), usuario));
    }
}
