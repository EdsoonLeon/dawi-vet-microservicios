package com.dawivet.veterinarios.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "veterinarios")
@Data
public class Veterinario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ID del usuario en auth-service (ya no hay relacion JPA)
    @Column(nullable = false, unique = true)
    private Integer usuarioId;

    // Copia de los datos del usuario para poder listar sin llamar a auth
    private String nombre;
    private String apellido;
    private String correo;

    private String especialidad;
    private String colegiatura;
    private Integer edad;

    @Column(nullable = false)
    private Boolean activo = true;
}