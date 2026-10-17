package com.dawivet.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.dawivet.auth.dto.RolReferenciaRequest;
import com.dawivet.auth.dto.UsuarioActualizacionRequest;
import com.dawivet.auth.dto.UsuarioRegistroRequest;
import com.dawivet.auth.model.Rol;
import com.dawivet.auth.model.Usuario;
import com.dawivet.auth.repository.IRolRepository;
import com.dawivet.auth.repository.IUsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final String CLAVE = "ClaveSegura123";

    @Mock
    private IUsuarioRepository usuarioRepository;

    @Mock
    private IRolRepository rolRepository;

    private PasswordEncoder passwordEncoder;
    private UsuarioService service;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        service = new UsuarioService(usuarioRepository, rolRepository, passwordEncoder);
    }

    @Test
    void registroNormalizaCorreoGeneraIdMantieneActivoYCodificaClave() {
        Rol rol = rol(3, "VETERINARIO");
        when(rolRepository.findById(3)).thenReturn(Optional.of(rol));
        when(usuarioRepository.findByCorreoIgnoreCase("ana@vet.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any())).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            assertNull(usuario.getId());
            assertTrue(usuario.getActivo());
            assertEquals(rol, usuario.getRol());
            assertNotEquals(CLAVE, usuario.getClave());
            assertTrue(passwordEncoder.matches(CLAVE, usuario.getClave()));
            usuario.setId(10);
            return usuario;
        });

        Usuario resultado = service.registrarUsuario(new UsuarioRegistroRequest(
                "Ana", "López", "  ANA@VET.COM  ", CLAVE, new RolReferenciaRequest(3)));

        assertEquals(10, resultado.getId());
        assertEquals("ana@vet.com", resultado.getCorreo());
    }

    @Test
    void registroRechazaCorreoDuplicadoSinPersistir() {
        when(rolRepository.findById(3)).thenReturn(Optional.of(rol(3, "VETERINARIO")));
        when(usuarioRepository.findByCorreoIgnoreCase("ana@vet.com")).thenReturn(Optional.of(usuarioActivo()));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.registrarUsuario(registro()));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registroRechazaRolInexistente() {
        when(rolRepository.findById(3)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.registrarUsuario(registro()));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void registroRechazaClaveQueSuperaLimiteDeBCryptEnUtf8() {
        when(rolRepository.findById(3)).thenReturn(Optional.of(rol(3, "VETERINARIO")));
        when(usuarioRepository.findByCorreoIgnoreCase("ana@vet.com")).thenReturn(Optional.empty());
        String claveUnicode = "🔐".repeat(20);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.registrarUsuario(new UsuarioRegistroRequest(
                        "Ana", "López", "ana@vet.com", claveUnicode, new RolReferenciaRequest(3))));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void loginOcultaLaCausaCuandoCorreoNoExiste() {
        when(usuarioRepository.findByCorreoIgnoreCase("nadie@vet.com")).thenReturn(Optional.empty());

        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> service.login(" NADIE@VET.COM ", CLAVE));

        assertEquals("Credenciales inválidas", exception.getMessage());
    }

    @Test
    void loginOcultaLaCausaCuandoClaveEsIncorrecta() {
        Usuario usuario = usuarioActivo();
        usuario.setClave(passwordEncoder.encode("OtraClaveSegura123"));
        when(usuarioRepository.findByCorreoIgnoreCase("ana@vet.com")).thenReturn(Optional.of(usuario));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> service.login("ana@vet.com", CLAVE));

        assertEquals("Credenciales inválidas", exception.getMessage());
    }

    @Test
    void loginOcultaLaCausaCuandoUsuarioEstaInactivo() {
        Usuario usuario = usuarioActivo();
        usuario.setActivo(false);
        when(usuarioRepository.findByCorreoIgnoreCase("ana@vet.com")).thenReturn(Optional.of(usuario));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> service.login("ana@vet.com", CLAVE));

        assertEquals("Credenciales inválidas", exception.getMessage());
    }

    @Test
    void loginCorrectoDevuelveUsuario() {
        Usuario usuario = usuarioActivo();
        when(usuarioRepository.findByCorreoIgnoreCase("ana@vet.com")).thenReturn(Optional.of(usuario));

        assertSame(usuario, service.login(" ANA@VET.COM ", CLAVE));
    }

    @Test
    void actualizacionDeUsuarioInexistenteRetorna404() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.editarUsuario(99, actualizacion(null, null, null)));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void actualizacionRechazaCorreoDuplicadoEnOtraCuenta() {
        Usuario existente = usuarioActivo();
        when(usuarioRepository.findById(10)).thenReturn(Optional.of(existente));
        when(usuarioRepository.existsByCorreoIgnoreCaseAndIdNot("otra@vet.com", 10)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.editarUsuario(10, actualizacion(" OTRA@VET.COM ", null, null)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void actualizacionRechazaRolInexistente() {
        when(usuarioRepository.findById(10)).thenReturn(Optional.of(usuarioActivo()));
        when(rolRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.editarUsuario(10, actualizacion(null, null, new RolReferenciaRequest(99))));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void actualizacionSinClaveConservaHashYPropiedadesInternas() {
        Usuario existente = usuarioActivo();
        Integer id = existente.getId();
        String hash = existente.getClave();
        Boolean activo = existente.getActivo();
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(existente)).thenReturn(existente);

        Usuario resultado = service.editarUsuario(id,
                new UsuarioActualizacionRequest("Ana María", null, null, "", null));

        assertEquals(hash, resultado.getClave());
        assertEquals(id, resultado.getId());
        assertEquals(activo, resultado.getActivo());
        assertEquals("Ana María", resultado.getNombre());
    }

    @Test
    void actualizacionCodificaNuevaClave() {
        Usuario existente = usuarioActivo();
        when(usuarioRepository.findById(10)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(existente)).thenReturn(existente);

        Usuario resultado = service.editarUsuario(10,
                new UsuarioActualizacionRequest(null, null, null, "NuevaClaveSegura123", null));

        assertNotEquals("NuevaClaveSegura123", resultado.getClave());
        assertTrue(passwordEncoder.matches("NuevaClaveSegura123", resultado.getClave()));
    }

    @Test
    void actualizacionRechazaNuevaClaveCorta() {
        Usuario existente = usuarioActivo();
        when(usuarioRepository.findById(10)).thenReturn(Optional.of(existente));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.editarUsuario(10,
                        new UsuarioActualizacionRequest(null, null, null, "corta", null)));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(usuarioRepository, never()).save(any());
    }

    private UsuarioRegistroRequest registro() {
        return new UsuarioRegistroRequest("Ana", "López", "ana@vet.com", CLAVE,
                new RolReferenciaRequest(3));
    }

    private UsuarioActualizacionRequest actualizacion(String correo, String clave, RolReferenciaRequest rol) {
        return new UsuarioActualizacionRequest(null, null, correo, clave, rol);
    }

    private Usuario usuarioActivo() {
        Usuario usuario = new Usuario();
        usuario.setId(10);
        usuario.setNombre("Ana");
        usuario.setApellido("López");
        usuario.setCorreo("ana@vet.com");
        usuario.setClave(passwordEncoder.encode(CLAVE));
        usuario.setActivo(true);
        usuario.setRol(rol(3, "VETERINARIO"));
        return usuario;
    }

    private Rol rol(int id, String nombre) {
        Rol rol = new Rol();
        rol.setId(id);
        rol.setNombre(nombre);
        return rol;
    }
}
