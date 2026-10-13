package com.dawivet.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dawivet.auth.dto.LoginRequest;
import com.dawivet.auth.dto.LoginResponse;
import com.dawivet.auth.model.Usuario;
import com.dawivet.auth.security.JwtUtil;
import com.dawivet.auth.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService service;
    private final JwtUtil jwtUtil;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@RequestBody Usuario nuevo) {
        try {
            Usuario creado = service.registrarUsuario(nuevo);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Ingresar los campos");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Usuario usuario = service.login(request.getCorreo(), request.getClave());
            String token = jwtUtil.generarToken(usuario);
            return ResponseEntity.ok(new LoginResponse(token, usuario));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}