package com.dawivet.mascotas.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dawivet.mascotas.model.Mascota;
import com.dawivet.mascotas.service.MascotaService;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    @Autowired
    private MascotaService service;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarMascota(@RequestBody Mascota nueva) {
        try {
            Mascota creada = service.registrarMascota(nueva);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> listarMascotas() {
        List<Mascota> mascotas = service.listarTodasMascotas();
        return ResponseEntity.ok(mascotas);
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<?> listarPorCliente(@PathVariable Integer clienteId) {
        List<Mascota> mascotas = service.listarPorCliente(clienteId);
        return ResponseEntity.ok(mascotas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerMascota(@PathVariable Integer id) {
        try {
            Mascota mascota = service.obtenerPorId(id);
            return ResponseEntity.ok(mascota);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarMascota(@PathVariable Integer id, @RequestBody Mascota datosNuevos) {
        try {
            Mascota actualizada = service.editarMascota(id, datosNuevos);
            return ResponseEntity.ok(actualizada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<?> activarMascota(@PathVariable Integer id) {
        try {
            Mascota mascota = service.activarMascota(id);
            return ResponseEntity.ok(mascota);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarMascota(@PathVariable Integer id) {
        try {
            service.eliminarMascota(id);
            return ResponseEntity.ok("Mascota eliminada correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}