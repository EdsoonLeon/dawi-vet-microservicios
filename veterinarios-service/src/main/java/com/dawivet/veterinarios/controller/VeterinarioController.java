package com.dawivet.veterinarios.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dawivet.veterinarios.dto.VeterinarioRequest;
import com.dawivet.veterinarios.model.Veterinario;
import com.dawivet.veterinarios.service.VeterinarioService;

@RestController
@RequestMapping("/api/veterinarios")
public class VeterinarioController {

    @Autowired
    private VeterinarioService service;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarVeterinario(@RequestBody VeterinarioRequest nuevo) {
        try {
            Veterinario creado = service.registrarVeterinario(nuevo);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> listarVeterinarios() {
        List<Veterinario> veterinarios = service.listarVeterinarios();
        return ResponseEntity.ok(veterinarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerVeterinario(@PathVariable Integer id) {
        try {
            Veterinario veterinario = service.obtenerPorId(id);
            return ResponseEntity.ok(veterinario);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarVeterinario(@PathVariable Integer id, @RequestBody Veterinario datosNuevos) {
        try {
            Veterinario actualizado = service.editarVeterinario(id, datosNuevos);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<?> activarVeterinario(@PathVariable Integer id) {
        try {
            Veterinario veterinario = service.activarVeterinario(id);
            return ResponseEntity.ok(veterinario);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarVeterinario(@PathVariable Integer id) {
        try {
            service.eliminarVeterinario(id);
            return ResponseEntity.ok("Veterinario eliminado correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}