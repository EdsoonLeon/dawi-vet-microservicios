package com.dawivet.vacunas.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "aplicaciones_vacunas", indexes = {
        @Index(name = "idx_aplicacion_mascota_fecha", columnList = "mascota_id,fecha_aplicacion"),
        @Index(name = "idx_aplicacion_veterinario", columnList = "veterinario_id"),
        @Index(name = "idx_aplicacion_vacuna", columnList = "vacuna_id"),
        @Index(name = "idx_aplicacion_estado_fecha", columnList = "estado,fecha_aplicacion"),
        @Index(name = "idx_aplicacion_mascota_nombre", columnList = "mascota_nombre")
})
@Getter
@Setter
@NoArgsConstructor
public class AplicacionVacuna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "vacuna_id", nullable = false)
    private Integer vacunaId;

    @Column(name = "mascota_id", nullable = false)
    private Integer mascotaId;

    @Column(name = "veterinario_id", nullable = false)
    private Integer veterinarioId;

    @Column(name = "fecha_aplicacion", nullable = false)
    private LocalDate fechaAplicacion;

    @Column(name = "proxima_dosis")
    private LocalDate proximaDosis;

    @Column(length = 100)
    private String lote;

    @Column(length = 500)
    private String observacion;

    @Column(name = "mascota_nombre", nullable = false, length = 120)
    private String mascotaNombre;

    @Column(name = "veterinario_nombre", nullable = false, length = 241)
    private String veterinarioNombre;

    @Column(name = "vacuna_nombre", nullable = false, length = 120)
    private String vacunaNombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAplicacion estado = EstadoAplicacion.REGISTRADA;

    @Column(name = "motivo_anulacion", length = 500)
    private String motivoAnulacion;

    @Column(name = "fecha_anulacion")
    private LocalDateTime fechaAnulacion;
}
