package com.dawivet.auth.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dawivet.auth.model.Usuario;
import com.dawivet.auth.repository.IUsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private IUsuarioRepository repoUsu;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario registrarUsuario(Usuario nuevo) {
        nuevo.setClave(passwordEncoder.encode(nuevo.getClave()));
        return repoUsu.save(nuevo);
    }

    // AHORA RECIBE CORREO Y CLAVE (LO ENVIA EL LoginRequest)
    public Usuario login(String correo, String clave) {
        Usuario encontrado = repoUsu.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no Encontrado"));

        if (!passwordEncoder.matches(clave, encontrado.getClave())) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        if (!encontrado.getActivo()) {
            throw new RuntimeException("Usuario inactivo");
        }
        return encontrado;
    }

    public List<Usuario> listarUsuarios() {
        return repoUsu.findAll();
    }

    public List<Usuario> listarUsuariosActivos() {
        return repoUsu.findByActivoTrue();
    }

    public Usuario obtnerPorid(Integer id) {
        return repoUsu.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public Usuario editarUsuario(Integer id, Usuario nuevoDatos) {
        Usuario existente = repoUsu.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        existente.setNombre(nuevoDatos.getNombre());
        existente.setApellido(nuevoDatos.getApellido());
        existente.setCorreo(nuevoDatos.getCorreo());
        existente.setRol(nuevoDatos.getRol());

        if (nuevoDatos.getClave() != null && !nuevoDatos.getClave().isBlank()) {
            existente.setClave(passwordEncoder.encode(nuevoDatos.getClave()));
        }
        return repoUsu.save(existente);
    }

    public void eliminarUsuario(Integer id) {
        Usuario usuario = obtnerPorid(id);
        usuario.setActivo(false);
        repoUsu.save(usuario);
    }

    public Usuario activarUsuario(Integer id) {
        Usuario usuario = obtnerPorid(id);
        usuario.setActivo(true);
        return repoUsu.save(usuario);
    }
}