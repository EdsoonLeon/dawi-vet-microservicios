package com.dawivet.clientes.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "clientes")
@Data
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    private String apellido;
    private String telefono;
    private String direccion;
    private String correo; // opcional

    @Column(nullable = false)
    private Boolean activo = true;
}