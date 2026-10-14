package com.dawivet.consultas.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dawivet.consultas.client.MascotaClient;
import com.dawivet.consultas.client.VeterinarioClient;
import com.dawivet.consultas.dto.MascotaDto;
import com.dawivet.consultas.dto.VeterinarioDto;
import com.dawivet.consultas.model.Consulta;
import com.dawivet.consultas.repository.IConsultaRepository;

import feign.FeignException;

@Service
public class ConsultaService {

    @Autowired
    private IConsultaRepository repoConsulta;

    @Autowired
    private MascotaClient mascotaClient;

    @Autowired
    private VeterinarioClient veterinarioClient;

    public Consulta registrarConsulta(Consulta nueva) {
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

        return repoConsulta.save(nueva);
    }

    public List<Consulta> listarConsultas() {
        return repoConsulta.findAll();
    }

    public List<Consulta> listarPorMascota(Integer mascotaId) {
        return repoConsulta.findByMascotaId(mascotaId);
    }

    public List<Consulta> listarPorVeterinario(Integer veterinarioId) {
        return repoConsulta.findByVeterinarioId(veterinarioId);
    }

    public Consulta obtenerPorId(Integer id) {
        return repoConsulta.findById(id)
                .orElseThrow(() -> new RuntimeException("Consulta no encontrada"));
    }

    public Consulta editarConsulta(Integer id, Consulta datosNuevos) {
        Consulta existente = repoConsulta.findById(id)
                .orElseThrow(() -> new RuntimeException("Consulta no encontrada"));

        existente.setFecha(datosNuevos.getFecha());
        existente.setDiagnostico(datosNuevos.getDiagnostico());
        existente.setTratamiento(datosNuevos.getTratamiento());
        existente.setObservacion(datosNuevos.getObservacion());

        return repoConsulta.save(existente);
    }

    public void eliminarConsulta(Integer id) {
        if (!repoConsulta.existsById(id)) {
            throw new RuntimeException("Consulta no encontrada");
        }
        repoConsulta.deleteById(id);
    }
}