package com.dawivet.auth.bootstrap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.dawivet.auth.model.Rol;
import com.dawivet.auth.model.Usuario;
import com.dawivet.auth.repository.IRolRepository;
import com.dawivet.auth.repository.IUsuarioRepository;

@Component
@ConditionalOnProperty(name = "dawivet.bootstrap.admin.enabled", havingValue = "true")
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private static final String ADMIN = "ADMIN";

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
        if (usuarioRepository.existsByRolNombre(ADMIN)) {
            log.info("Bootstrap ADMIN omitido: ya existe una cuenta administradora");
            return;
        }

        String nombre = requerido("dawivet.bootstrap.admin.nombre");
        String apellido = requerido("dawivet.bootstrap.admin.apellido");
        String correo = requerido("dawivet.bootstrap.admin.correo").toLowerCase(Locale.ROOT);
        String clave = leerClave(requerido("dawivet.bootstrap.admin.password-file"));

        if (!correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("El correo del administrador no es válido");
        }
        if (clave.length() < 12 || clave.isBlank()) {
            throw new IllegalArgumentException("La contraseña inicial debe tener al menos 12 caracteres");
        }
        if (usuarioRepository.findByCorreoIgnoreCase(correo).isPresent()) {
            throw new IllegalStateException("El correo indicado ya pertenece a otra cuenta");
        }

        Rol rol = rolRepository.findByNombre(ADMIN)
                .orElseThrow(() -> new IllegalStateException("El rol ADMIN no está configurado"));

        Usuario administrador = new Usuario();
        administrador.setNombre(nombre);
        administrador.setApellido(apellido);
        administrador.setCorreo(correo);
        administrador.setClave(passwordEncoder.encode(clave));
        administrador.setRol(rol);
        administrador.setActivo(true);
        usuarioRepository.save(administrador);

        log.info("Bootstrap ADMIN completado correctamente");
    }

    private String requerido(String propiedad) {
        String valor = environment.getProperty(propiedad);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta la configuración requerida: " + propiedad);
        }
        return valor.trim();
    }

    private String leerClave(String archivo) {
        try {
            return Files.readString(Path.of(archivo), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer el archivo de contraseña", e);
        }
    }
}
