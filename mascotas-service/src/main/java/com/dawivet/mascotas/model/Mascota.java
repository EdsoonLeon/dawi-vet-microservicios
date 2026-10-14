package com.dawivet.mascotas.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "mascotas")
@Data
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    private String especie;
    private String raza;
    private Integer edad;

    @Column(nullable = false)
    private Boolean activo = true;

    // ID del cliente en clientes-service (ya no hay relacion JPA)
    @Column(nullable = false)
    private Integer clienteId;

    // Copia del nombre del dueño para listar sin llamar a clientes
    private String clienteNombre;
}