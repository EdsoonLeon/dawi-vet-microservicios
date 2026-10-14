package com.dawivet.citas.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "citas")
@Data
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDate fecha;
    private LocalTime hora;

    @Column(nullable = false)
    private String estado = "PENDIENTE";

    // ID de la mascota en mascotas-service (ya no hay relacion JPA)
    @Column(nullable = false)
    private Integer mascotaId;
    private String mascotaNombre;

    // ID del veterinario en veterinarios-service (ya no hay relacion JPA)
    @Column(nullable = false)
    private Integer veterinarioId;
    private String veterinarioNombre;
}