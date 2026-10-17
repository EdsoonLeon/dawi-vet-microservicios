package com.dawivet.auth.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dawivet.auth.model.Rol;
import com.dawivet.auth.model.Usuario;
import com.dawivet.auth.repository.IRolRepository;
import com.dawivet.auth.repository.IUsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapTest {

    private static final String CLAVE = "ClaveInicialSegura123";

    @Mock
    private IUsuarioRepository usuarioRepository;

    @Mock
    private IRolRepository rolRepository;

    @TempDir
    private Path tempDir;

    private PasswordEncoder passwordEncoder;
    private Path passwordFile;

    @BeforeEach
    void setUp() throws IOException {
        passwordEncoder = new BCryptPasswordEncoder();
        passwordFile = tempDir.resolve("admin-password");
        Files.writeString(passwordFile, CLAVE);
    }

    @Test
    void creaElPrimerAdministradorConRolPorNombreYClaveBCrypt() {
        Rol admin = rolAdmin();
        when(usuarioRepository.existsByRolNombre("ADMIN")).thenReturn(false);
        when(usuarioRepository.findByCorreoIgnoreCase("admin@dawivet.test")).thenReturn(Optional.empty());
        when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.of(admin));

        bootstrap(entornoValido()).run(mock(ApplicationArguments.class));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertEquals("Administradora", guardado.getNombre());
        assertEquals("Inicial", guardado.getApellido());
        assertEquals("admin@dawivet.test", guardado.getCorreo());
        assertEquals(admin, guardado.getRol());
        assertTrue(guardado.getActivo());
        assertNotEquals(CLAVE, guardado.getClave());
        assertTrue(passwordEncoder.matches(CLAVE, guardado.getClave()));
    }

    @Test
    void administradorExistenteNoSeModificaAunqueFaltenCredenciales() {
        when(usuarioRepository.existsByRolNombre("ADMIN")).thenReturn(true);

        bootstrap(new MockEnvironment()).run(mock(ApplicationArguments.class));

        verify(usuarioRepository, never()).save(any());
        verifyNoInteractions(rolRepository);
    }

    @Test
    void ejecucionRepetidaNoDuplicaAdministradores() {
        when(usuarioRepository.existsByRolNombre("ADMIN")).thenReturn(false, true);
        when(usuarioRepository.findByCorreoIgnoreCase("admin@dawivet.test")).thenReturn(Optional.empty());
        when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.of(rolAdmin()));
        AdminBootstrap bootstrap = bootstrap(entornoValido());

        bootstrap.run(mock(ApplicationArguments.class));
        bootstrap.run(mock(ApplicationArguments.class));

        verify(usuarioRepository).save(any());
    }

    @Test
    void credencialesAusentesSonRechazadas() {
        when(usuarioRepository.existsByRolNombre("ADMIN")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> bootstrap(new MockEnvironment()).run(mock(ApplicationArguments.class)));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void claveCortaEsRechazada() throws IOException {
        Files.writeString(passwordFile, "corta");
        when(usuarioRepository.existsByRolNombre("ADMIN")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> bootstrap(entornoValido()).run(mock(ApplicationArguments.class)));
        verify(usuarioRepository, never()).save(any());
    }

    private AdminBootstrap bootstrap(MockEnvironment environment) {
        return new AdminBootstrap(usuarioRepository, rolRepository, passwordEncoder, environment);
    }

    private MockEnvironment entornoValido() {
        return new MockEnvironment()
                .withProperty("dawivet.bootstrap.admin.nombre", "Administradora")
                .withProperty("dawivet.bootstrap.admin.apellido", "Inicial")
                .withProperty("dawivet.bootstrap.admin.correo", "ADMIN@DAWIVET.TEST")
                .withProperty("dawivet.bootstrap.admin.password-file", passwordFile.toString());
    }

    private Rol rolAdmin() {
        Rol rol = new Rol();
        rol.setId(27);
        rol.setNombre("ADMIN");
        return rol;
    }
}
