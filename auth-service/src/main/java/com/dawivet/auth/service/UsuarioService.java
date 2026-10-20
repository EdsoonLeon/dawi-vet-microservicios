package com.dawivet.auth.service;

import static com.dawivet.auth.util.CorreoNormalizador.normalizar;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.dawivet.auth.dto.UsuarioActualizacionRequest;
import com.dawivet.auth.dto.UsuarioRegistroRequest;
import com.dawivet.auth.model.Rol;
import com.dawivet.auth.model.Usuario;
import com.dawivet.auth.repository.IRolRepository;
import com.dawivet.auth.repository.IUsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final IUsuarioRepository repoUsu;
    private final IRolRepository repoRol;
    private final PasswordEncoder passwordEncoder;

    public Usuario registrarUsuario(UsuarioRegistroRequest request) {
        Rol rol = repoRol.findById(request.rol().id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no encontrado"));
        String correo = normalizar(request.correo());
        validarCorreoDisponible(correo, null);
        validarNuevaClave(request.clave());

        Usuario nuevo = new Usuario();
        nuevo.setNombre(request.nombre());
        nuevo.setApellido(request.apellido());
        nuevo.setCorreo(correo);
        nuevo.setClave(passwordEncoder.encode(request.clave()));
        nuevo.setRol(rol);
        nuevo.setActivo(true);
        return repoUsu.save(nuevo);
    }

    public Usuario login(String correo, String clave) {
        Usuario encontrado = repoUsu.findByCorreoIgnoreCase(normalizar(correo))
                .orElseThrow(this::credencialesInvalidas);

        if (clave.getBytes(StandardCharsets.UTF_8).length > 72
                || !passwordEncoder.matches(clave, encontrado.getClave())
                || !Boolean.TRUE.equals(encontrado.getActivo())) {
            throw credencialesInvalidas();
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
        return repoUsu.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    public Usuario editarUsuario(Integer id, UsuarioActualizacionRequest request) {
        Usuario existente = obtnerPorid(id);

        if (request.nombre() != null) {
            existente.setNombre(request.nombre());
        }
        if (request.apellido() != null) {
            existente.setApellido(request.apellido());
        }
        if (request.correo() != null) {
            if (request.correo().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo no puede estar vacío");
            }
            String correo = normalizar(request.correo());
            validarCorreoDisponible(correo, id);
            existente.setCorreo(correo);
        }
        if (request.rol() != null) {
            Rol rol = repoRol.findById(request.rol().id())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no encontrado"));
            existente.setRol(rol);
        }

        if (request.clave() != null && !request.clave().isBlank()) {
            validarNuevaClave(request.clave());
            existente.setClave(passwordEncoder.encode(request.clave()));
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

    private void validarCorreoDisponible(String correo, Integer usuarioId) {
        boolean duplicado = usuarioId == null
                ? repoUsu.findByCorreoIgnoreCase(correo).isPresent()
                : repoUsu.existsByCorreoIgnoreCaseAndIdNot(correo, usuarioId);
        if (duplicado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
    }

    private void validarNuevaClave(String clave) {
        if (clave == null || clave.isBlank() || clave.length() < 8
                || clave.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña debe tener al menos 8 caracteres y no superar 72 bytes");
        }
    }

    private BadCredentialsException credencialesInvalidas() {
        return new BadCredentialsException("Credenciales inválidas");
    }
}
