package com.dawivet.veterinarios.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dawivet.veterinarios.client.AuthClient;
import com.dawivet.veterinarios.dto.RolDto;
import com.dawivet.veterinarios.dto.UsuarioDatos;
import com.dawivet.veterinarios.dto.UsuarioDto;
import com.dawivet.veterinarios.dto.UsuarioRegistroDto;
import com.dawivet.veterinarios.dto.VeterinarioRequest;
import com.dawivet.veterinarios.model.Veterinario;
import com.dawivet.veterinarios.repository.IVeterinarioRepository;

@Service
public class VeterinarioService {

    @Autowired
    private IVeterinarioRepository repoVeterinario;

    @Autowired
    private AuthClient authClient;

    public Veterinario registrarVeterinario(VeterinarioRequest request) {
        UsuarioDatos datos = request.getUsuario();

        // 1. Buscar el rol VETERINARIO en auth-service
        RolDto rol = authClient.listarRoles().stream()
                .filter(r -> "VETERINARIO".equals(r.getNombre()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Rol VETERINARIO no configurado"));

        // 2. Crear el usuario en auth-service
        UsuarioRegistroDto nuevoUsuario = new UsuarioRegistroDto(
                datos.getNombre(), datos.getApellido(), datos.getCorreo(), datos.getClave(), rol);
        UsuarioDto usuarioCreado = authClient.registrar(nuevoUsuario);

        // 3. Guardar el veterinario con el id del usuario
        Veterinario veterinario = new Veterinario();
        veterinario.setUsuarioId(usuarioCreado.getId());
        veterinario.setNombre(datos.getNombre());
        veterinario.setApellido(datos.getApellido());
        veterinario.setCorreo(datos.getCorreo());
        veterinario.setEspecialidad(request.getEspecialidad());
        veterinario.setColegiatura(request.getColegiatura());
        veterinario.setEdad(request.getEdad());

        return repoVeterinario.save(veterinario);
    }

    public List<Veterinario> listarVeterinarios() {
        return repoVeterinario.findByActivoTrue();
    }

    public Veterinario obtenerPorId(Integer id) {
        return repoVeterinario.findById(id)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));
    }

    public Veterinario editarVeterinario(Integer id, Veterinario datosNuevos) {
        Veterinario existente = repoVeterinario.findById(id)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));

        existente.setEspecialidad(datosNuevos.getEspecialidad());
        existente.setColegiatura(datosNuevos.getColegiatura());
        existente.setEdad(datosNuevos.getEdad());

        return repoVeterinario.save(existente);
    }

    public void eliminarVeterinario(Integer id) {
        Veterinario veterinario = repoVeterinario.findById(id)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));
        veterinario.setActivo(false);
        repoVeterinario.save(veterinario);
    }

    public Veterinario activarVeterinario(Integer id) {
        Veterinario veterinario = repoVeterinario.findById(id)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));
        veterinario.setActivo(true);
        return repoVeterinario.save(veterinario);
    }
}