package com.dawivet.clientes.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dawivet.clientes.model.Cliente;
import com.dawivet.clientes.repository.IClienteRepository;

@Service
public class ClienteService {

    @Autowired
    private IClienteRepository repoCliente;

    public Cliente registrarCliente(Cliente nuevo) {
        return repoCliente.save(nuevo);
    }

    public List<Cliente> listarClientes() {
        return repoCliente.findAll();
    }

    public List<Cliente> listarClientesActivos() {
        return repoCliente.findByActivoTrue();
    }

    public Cliente obtenerPorId(Integer id) {
        return repoCliente.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }

    public Cliente editarCliente(Integer id, Cliente datosNuevos) {
        Cliente existente = repoCliente.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        existente.setNombre(datosNuevos.getNombre());
        existente.setApellido(datosNuevos.getApellido());
        existente.setTelefono(datosNuevos.getTelefono());
        existente.setDireccion(datosNuevos.getDireccion());
        existente.setCorreo(datosNuevos.getCorreo());

        return repoCliente.save(existente);
    }

    // metodo para activar el cliente desde el angular
    public Cliente activarCliente(Integer id) {
        Cliente cliente = repoCliente.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        cliente.setActivo(true);
        return repoCliente.save(cliente);
    }

    public void eliminarCliente(Integer id) {
        Cliente cliente = repoCliente.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        cliente.setActivo(false);
        repoCliente.save(cliente);
    }
}