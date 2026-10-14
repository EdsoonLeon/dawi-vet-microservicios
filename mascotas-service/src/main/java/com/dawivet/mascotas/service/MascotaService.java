package com.dawivet.mascotas.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dawivet.mascotas.client.ClienteClient;
import com.dawivet.mascotas.dto.ClienteDto;
import com.dawivet.mascotas.model.Mascota;
import com.dawivet.mascotas.repository.IMascotaRepository;

import feign.FeignException;

@Service
public class MascotaService {

    @Autowired
    private IMascotaRepository repoMascota;

    @Autowired
    private ClienteClient clienteClient;

    public Mascota registrarMascota(Mascota nueva) {
        // Verifica en clientes-service que el dueño exista
        ClienteDto cliente;
        try {
            cliente = clienteClient.obtenerPorId(nueva.getClienteId());
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Cliente no encontrado");
        }

        nueva.setId(null);
        nueva.setClienteNombre(cliente.getNombre() + " " + cliente.getApellido());
        return repoMascota.save(nueva);
    }

    public List<Mascota> listarTodasMascotas() {
        return repoMascota.findAll();
    }

    public List<Mascota> listarMascotas() {
        return repoMascota.findByActivoTrue();
    }

    public List<Mascota> listarPorCliente(Integer clienteId) {
        return repoMascota.findByClienteId(clienteId);
    }

    public Mascota obtenerPorId(Integer id) {
        return repoMascota.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
    }

    public Mascota editarMascota(Integer id, Mascota datosNuevos) {
        Mascota existente = repoMascota.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
 
        existente.setNombre(datosNuevos.getNombre());
        existente.setEspecie(datosNuevos.getEspecie());
        existente.setRaza(datosNuevos.getRaza());
        existente.setEdad(datosNuevos.getEdad());

        return repoMascota.save(existente);
    }

    public void eliminarMascota(Integer id) {
        Mascota mascota = repoMascota.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
        mascota.setActivo(false);
        repoMascota.save(mascota);
    }

    public Mascota activarMascota(Integer id) {
        Mascota mascota = repoMascota.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
        mascota.setActivo(true);
        return repoMascota.save(mascota);
    }
}