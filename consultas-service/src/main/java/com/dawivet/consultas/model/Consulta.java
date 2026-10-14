package com.dawivet.consultas.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "consultas")
@Data
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDate fecha;
    private String diagnostico;
    private String tratamiento;
    private String observacion;

    // ID de la mascota en mascotas-service (ya no hay relacion JPA)
    @Column(nullable = false)
    private Integer mascotaId;
    private String mascotaNombre;

    // ID del veterinario en veterinarios-service (ya no hay relacion JPA)
    @Column(nullable = false)
    private Integer veterinarioId;
    private String veterinarioNombre;
}