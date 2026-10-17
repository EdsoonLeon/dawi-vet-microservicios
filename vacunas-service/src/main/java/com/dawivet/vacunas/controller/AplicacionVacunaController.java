package com.dawivet.vacunas.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dawivet.vacunas.dto.AnulacionRequest;
import com.dawivet.vacunas.dto.ApiResponse;
import com.dawivet.vacunas.dto.AplicacionVacunaRequest;
import com.dawivet.vacunas.dto.AplicacionVacunaResponse;
import com.dawivet.vacunas.dto.AplicacionVacunaUpdateRequest;
import com.dawivet.vacunas.dto.PageResponse;
import com.dawivet.vacunas.model.EstadoAplicacion;
import com.dawivet.vacunas.service.AplicacionVacunaService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vacunas/aplicaciones")
@RequiredArgsConstructor
@Validated
public class AplicacionVacunaController {

    private final AplicacionVacunaService service;

    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<AplicacionVacunaResponse>> registrar(
            @Valid @RequestBody AplicacionVacunaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Aplicación de vacuna registrada correctamente", service.registrar(request)));
    }

    @GetMapping
    public ApiResponse<PageResponse<AplicacionVacunaResponse>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nombreMascota,
            @RequestParam(required = false) Integer mascotaId,
            @RequestParam(required = false) Integer veterinarioId,
            @RequestParam(required = false) Integer vacunaId,
            @RequestParam(required = false) EstadoAplicacion estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaHasta,
            @RequestParam(defaultValue = "fechaAplicacion") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return ApiResponse.success("Historial de vacunación obtenido correctamente",
                service.buscar(page, size, nombreMascota, mascotaId, veterinarioId, vacunaId,
                        estado, fechaDesde, fechaHasta, sortBy, direction));
    }

    @GetMapping("/{id}")
    public ApiResponse<AplicacionVacunaResponse> obtener(
            @PathVariable @Positive(message = "El ID debe ser positivo") Integer id) {
        return ApiResponse.success("Aplicación de vacuna obtenida correctamente", service.obtener(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<AplicacionVacunaResponse> actualizar(
            @PathVariable @Positive(message = "El ID debe ser positivo") Integer id,
            @Valid @RequestBody AplicacionVacunaUpdateRequest request) {
        return ApiResponse.success(
                "Aplicación de vacuna corregida correctamente", service.actualizar(id, request));
    }

    @PatchMapping("/{id}/anular")
    public ApiResponse<AplicacionVacunaResponse> anular(
            @PathVariable @Positive(message = "El ID debe ser positivo") Integer id,
            @Valid @RequestBody AnulacionRequest request) {
        return ApiResponse.success(
                "Aplicación de vacuna anulada correctamente", service.anular(id, request.motivo()));
    }
}
