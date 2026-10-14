package com.dawivet.citas.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dawivet.citas.model.Cita;
import com.dawivet.citas.service.CitaService;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaService service;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarCita(@RequestBody Cita nueva) {
        try {
            Cita creada = service.registrarCita(nueva);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> listarCitas() {
        return ResponseEntity.ok(service.listarCitas());
    }

    @GetMapping("/mascota/{mascotaId}")
    public ResponseEntity<?> listarPorMascota(@PathVariable Integer mascotaId) {
        return ResponseEntity.ok(service.listarPorMascota(mascotaId));
    }

    @GetMapping("/veterinario/{veterinarioId}")
    public ResponseEntity<?> listarPorVeterinario(@PathVariable Integer veterinarioId) {
        return ResponseEntity.ok(service.listarPorVeterinario(veterinarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerCita(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.obtenerPorId(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarCita(@PathVariable Integer id, @RequestBody Cita datosNuevos) {
        try {
            return ResponseEntity.ok(service.editarCita(id, datosNuevos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        try {
            String nuevoEstado = body.get("estado");
            return ResponseEntity.ok(service.cambiarEstado(id, nuevoEstado));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelarCita(@PathVariable Integer id) {
        try {
            service.cancelarCita(id);
            return ResponseEntity.ok("Cita cancelada correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}