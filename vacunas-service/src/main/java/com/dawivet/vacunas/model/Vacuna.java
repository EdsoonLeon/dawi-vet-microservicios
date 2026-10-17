package com.dawivet.vacunas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vacunas", uniqueConstraints = {
        @UniqueConstraint(name = "uk_vacunas_nombre", columnNames = "nombre")
})
@Getter
@Setter
@NoArgsConstructor
public class Vacuna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "enfermedad_previene", nullable = false, length = 200)
    private String enfermedadPreviene;

    @Column(nullable = false)
    private Boolean activo = true;
}
