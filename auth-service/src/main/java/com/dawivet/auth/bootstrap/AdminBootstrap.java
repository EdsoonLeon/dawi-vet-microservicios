package com.dawivet.auth.bootstrap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.dawivet.auth.model.Rol;
import com.dawivet.auth.model.Usuario;
import com.dawivet.auth.repository.IRolRepository;
import com.dawivet.auth.repository.IUsuarioRepository;
import com.dawivet.auth.util.CorreoNormalizador;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private static final String ADMIN = "ADMIN";
    private static final String VETERINARIO = "VETERINARIO";
    private static final String RECEPCIONISTA = "RECEPCIONISTA";
    private static final String BOOTSTRAP_ENABLED = "dawivet.bootstrap.admin.enabled";

    private final IUsuarioRepository usuarioRepository;
    private final IRolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public AdminBootstrap(IUsuarioRepository usuarioRepository, IRolRepository rolRepository,
            PasswordEncoder passwordEncoder, Environment environment) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Rol rolAdmin = inicializarRoles();

        if (environment.getProperty(BOOTSTRAP_ENABLED, Boolean.class, false)) {
            crearPrimerAdministrador(rolAdmin);
        }
        crearAdministradorInicial(rolAdmin);
    }

    private Rol inicializarRoles() {
        Rol rolAdmin = obtenerOCrearRol(ADMIN);
        obtenerOCrearRol(VETERINARIO);
        obtenerOCrearRol(RECEPCIONISTA);
        return rolAdmin;
    }

    private Rol obtenerOCrearRol(String nombre) {
        return rolRepository.findByNombre(nombre).orElseGet(() -> {
            Rol rol = new Rol();
            rol.setNombre(nombre);
            Rol guardado = rolRepository.save(rol);
            log.info("Rol {} creado", nombre);
            return guardado;
        });
    }

    private void crearPrimerAdministrador(Rol rolAdmin) {
        if (usuarioRepository.existsByRolNombre(ADMIN)) {
            log.info("Bootstrap ADMIN omitido: ya existe una cuenta administradora");
            return;
        }

        String nombre = requerido("dawivet.bootstrap.admin.nombre");
        String apellido = requerido("dawivet.bootstrap.admin.apellido");
        String correo = CorreoNormalizador.normalizar(requerido("dawivet.bootstrap.admin.correo"));
        String clave = leerClave(requerido("dawivet.bootstrap.admin.password-file"));

        validarCorreo(correo);
        if (clave.length() < 8 || clave.isBlank()) {
            throw new IllegalArgumentException("La contraseña inicial debe tener al menos 8 caracteres");
        }
        if (usuarioRepository.findByCorreoIgnoreCase(correo).isPresent()) {
            throw new IllegalStateException("El correo indicado ya pertenece a otra cuenta");
        }

        Usuario administrador = new Usuario();
        administrador.setNombre(nombre);
        administrador.setApellido(apellido);
        administrador.setCorreo(correo);
        administrador.setClave(passwordEncoder.encode(clave));
        administrador.setRol(rolAdmin);
        administrador.setActivo(true);
        usuarioRepository.save(administrador);

        log.info("Bootstrap ADMIN completado correctamente");
    }

    private void crearAdministradorInicial(Rol rolAdmin) {
        String correo = CorreoNormalizador.normalizar(requerido("dawivet.admin.email"));
        String clave = requeridoSinRecortar("dawivet.admin.password");
        validarCorreo(correo);
        if (clave.length() < 8 || clave.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "La contraseña inicial debe tener al menos 8 caracteres y no superar 72 bytes");
        }

        Usuario existente = usuarioRepository.findByCorreoIgnoreCase(correo).orElse(null);
        if (existente != null) {
            if (existente.getRol() == null || !ADMIN.equals(existente.getRol().getNombre())) {
                log.warn("Administrador inicial no creado: el correo configurado pertenece a otro rol");
            } else {
                log.info("Administrador inicial omitido: la cuenta ya existe");
            }
            return;
        }

        Usuario administrador = new Usuario();
        administrador.setNombre("admin");
        administrador.setApellido("admin");
        administrador.setCorreo(correo);
        administrador.setClave(passwordEncoder.encode(clave));
        administrador.setRol(rolAdmin);
        administrador.setActivo(true);
        usuarioRepository.save(administrador);

        log.info("Administrador inicial creado correctamente");
    }

    private String requerido(String propiedad) {
        String valor = environment.getProperty(propiedad);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta la configuración requerida: " + propiedad);
        }
        return valor.trim();
    }

    private String requeridoSinRecortar(String propiedad) {
        String valor = environment.getProperty(propiedad);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta la configuración requerida: " + propiedad);
        }
        return valor;
    }

    private void validarCorreo(String correo) {
        if (correo.length() > 255 || !correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("El correo del administrador no es válido");
        }
    }

    private String leerClave(String archivo) {
        try {
            return Files.readString(Path.of(archivo), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer el archivo de contraseña", e);
        }
    }
}
