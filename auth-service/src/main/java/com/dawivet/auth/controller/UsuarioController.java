package com.dawivet.auth.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.dawivet.auth.dto.UsuarioActualizacionRequest;
import com.dawivet.auth.model.Usuario;
import com.dawivet.auth.service.UsuarioService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios")
@Validated
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    @GetMapping
    public ResponseEntity<?> listarUsuarios() {
        List<Usuario> usuarios = service.listarUsuariosActivos();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable @Positive Integer id) {
        return ResponseEntity.ok(service.obtnerPorid(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> editarUsuario(@PathVariable @Positive Integer id,
            @Valid @RequestBody UsuarioActualizacionRequest request) {
        return ResponseEntity.ok(service.editarUsuario(id, request));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Usuario> activarUsuario(@PathVariable @Positive Integer id) {
        return ResponseEntity.ok(service.activarUsuario(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarUsuario(@PathVariable @Positive Integer id) {
        service.eliminarUsuario(id);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }
}
