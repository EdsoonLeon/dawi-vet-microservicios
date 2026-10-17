package com.dawivet.vacunas.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dawivet.vacunas.dto.ApiResponse;
import com.dawivet.vacunas.dto.PageResponse;
import com.dawivet.vacunas.dto.VacunaRequest;
import com.dawivet.vacunas.dto.VacunaResponse;
import com.dawivet.vacunas.service.VacunaService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vacunas")
@RequiredArgsConstructor
@Validated
public class VacunaController {

    private final VacunaService service;

    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<VacunaResponse>> registrar(@Valid @RequestBody VacunaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vacuna registrada correctamente", service.registrar(request)));
    }

    @GetMapping
    public ApiResponse<PageResponse<VacunaResponse>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return ApiResponse.success("Vacunas obtenidas correctamente",
                service.buscar(page, size, nombre, activo, sortBy, direction));
    }

    @GetMapping("/{id}")
    public ApiResponse<VacunaResponse> obtener(
            @PathVariable @Positive(message = "El ID debe ser positivo") Integer id) {
        return ApiResponse.success("Vacuna obtenida correctamente", service.obtener(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<VacunaResponse> actualizar(
            @PathVariable @Positive(message = "El ID debe ser positivo") Integer id,
            @Valid @RequestBody VacunaRequest request) {
        return ApiResponse.success("Vacuna actualizada correctamente", service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<VacunaResponse> desactivar(
            @PathVariable @Positive(message = "El ID debe ser positivo") Integer id) {
        return ApiResponse.success("Vacuna desactivada correctamente", service.desactivar(id));
    }

    @PatchMapping("/{id}/activar")
    public ApiResponse<VacunaResponse> activar(
            @PathVariable @Positive(message = "El ID debe ser positivo") Integer id) {
        return ApiResponse.success("Vacuna activada correctamente", service.activar(id));
    }
}
