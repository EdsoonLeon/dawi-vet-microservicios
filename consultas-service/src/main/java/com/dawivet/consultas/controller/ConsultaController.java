package com.dawivet.consultas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dawivet.consultas.model.Consulta;
import com.dawivet.consultas.service.ConsultaService;

@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    @Autowired
    private ConsultaService service;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarConsulta(@RequestBody Consulta nueva) {
        try {
            Consulta creada = service.registrarConsulta(nueva);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> listarConsultas() {
        return ResponseEntity.ok(service.listarConsultas());
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
    public ResponseEntity<?> obtenerConsulta(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.obtenerPorId(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarConsulta(@PathVariable Integer id, @RequestBody Consulta datosNuevos) {
        try {
            return ResponseEntity.ok(service.editarConsulta(id, datosNuevos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarConsulta(@PathVariable Integer id) {
        try {
            service.eliminarConsulta(id);
            return ResponseEntity.ok("Consulta eliminada correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}