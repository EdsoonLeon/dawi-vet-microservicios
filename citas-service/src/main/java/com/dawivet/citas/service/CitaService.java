package com.dawivet.citas.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dawivet.citas.client.MascotaClient;
import com.dawivet.citas.client.VeterinarioClient;
import com.dawivet.citas.dto.MascotaDto;
import com.dawivet.citas.dto.VeterinarioDto;
import com.dawivet.citas.model.Cita;
import com.dawivet.citas.repository.ICitaRepository;

import feign.FeignException;

@Service
public class CitaService {

    @Autowired
    private ICitaRepository repoCita;

    @Autowired
    private MascotaClient mascotaClient;

    @Autowired
    private VeterinarioClient veterinarioClient;

    public Cita registrarCita(Cita nueva) {
        // Verifica en mascotas-service que la mascota exista
        MascotaDto mascota;
        try {
            mascota = mascotaClient.obtenerPorId(nueva.getMascotaId());
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Mascota no encontrada");
        }

        // Verifica en veterinarios-service que el veterinario exista
        VeterinarioDto veterinario;
        try {
            veterinario = veterinarioClient.obtenerPorId(nueva.getVeterinarioId());
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Veterinario no encontrado");
        }

        nueva.setId(null);
        nueva.setMascotaNombre(mascota.getNombre());
        nueva.setVeterinarioNombre(veterinario.getNombre() + " " + veterinario.getApellido());
        nueva.setEstado("PENDIENTE");

        return repoCita.save(nueva);
    }

    public List<Cita> listarCitas() {
        return repoCita.findAll();
    }

    public List<Cita> listarPorMascota(Integer mascotaId) {
        return repoCita.findByMascotaId(mascotaId);
    }

    public List<Cita> listarPorVeterinario(Integer veterinarioId) {
        return repoCita.findByVeterinarioId(veterinarioId);
    }

    public Cita obtenerPorId(Integer id) {
        return repoCita.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
    }

    public Cita editarCita(Integer id, Cita datosNuevos) {
        Cita existente = repoCita.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        existente.setFecha(datosNuevos.getFecha());
        existente.setHora(datosNuevos.getHora());

        return repoCita.save(existente);
    }

    public Cita cambiarEstado(Integer id, String nuevoEstado) {
        Cita cita = repoCita.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        cita.setEstado(nuevoEstado);
        return repoCita.save(cita);
    }

    public void cancelarCita(Integer id) {
        cambiarEstado(id, "CANCELADA");
    }
}