package com.dawivet.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.dawivet.auth.exception.ApiExceptionHandler;
import com.dawivet.auth.model.Rol;
import com.dawivet.auth.model.Usuario;
import com.dawivet.auth.security.JwtFilter;
import com.dawivet.auth.security.JwtUtil;
import com.dawivet.auth.security.SecurityConfig;
import com.dawivet.auth.service.UsuarioService;

import io.jsonwebtoken.Claims;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtFilter.class, ApiExceptionHandler.class})
class AuthValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void registroRechazaCorreoNuloVacioOInvalido() throws Exception {
        String[] correos = {"null", "\"   \"", "\"correo-invalido\""};

        for (String correo : correos) {
            mockMvc.perform(post("/api/auth/registrar")
                            .header("Authorization", bearerAdmin())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registroJson(correo, "\"ClaveSegura123\"", "{\"id\":3}")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Datos de entrada inválidos"));
        }

        verifyNoInteractions(usuarioService);
    }

    @Test
    void registroRechazaClaveNulaVaciaOCorta() throws Exception {
        String[] claves = {"null", "\"   \"", "\"corta\""};

        for (String clave : claves) {
            mockMvc.perform(post("/api/auth/registrar")
                            .header("Authorization", bearerAdmin())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registroJson("\"ana@vet.com\"", clave, "{\"id\":3}")))
                    .andExpect(status().isBadRequest());
        }

        verifyNoInteractions(usuarioService);
    }

    @Test
    void registroRechazaRolNuloOConIdNoPositivo() throws Exception {
        String[] roles = {"null", "{\"id\":null}", "{\"id\":0}"};

        for (String rol : roles) {
            mockMvc.perform(post("/api/auth/registrar")
                            .header("Authorization", bearerAdmin())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registroJson("\"ana@vet.com\"", "\"ClaveSegura123\"", rol)))
                    .andExpect(status().isBadRequest());
        }

        verifyNoInteractions(usuarioService);
    }

    @Test
    void registroMapeaRolInexistenteA400YCorreoDuplicadoA409() throws Exception {
        when(usuarioService.registrarUsuario(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no encontrado"))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado"));

        mockMvc.perform(post("/api/auth/registrar")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson("\"ana@vet.com\"", "\"ClaveSegura123\"", "{\"id\":3}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Rol no encontrado"));

        mockMvc.perform(post("/api/auth/registrar")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson("\"ana@vet.com\"", "\"ClaveSegura123\"", "{\"id\":3}")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("El correo ya está registrado"));
    }

    @Test
    void errorDeIntegridadNoExponeMensajeSqlNiLoAsumeComoCorreoDuplicado() throws Exception {
        when(usuarioService.registrarUsuario(any()))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry SQL interno"));

        mockMvc.perform(post("/api/auth/registrar")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson("\"ana@vet.com\"", "\"ClaveSegura123\"", "{\"id\":3}")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Conflicto con los datos existentes"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("SQL interno"))));
    }

    @Test
    void loginRechazaCamposInvalidos() throws Exception {
        String[] cuerpos = {
                "{}",
                "{\"correo\":\"correo-invalido\",\"clave\":\"x\"}",
                "{\"correo\":\"ana@vet.com\",\"clave\":\"   \"}"
        };

        for (String cuerpo : cuerpos) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cuerpo))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Datos de entrada inválidos"));
        }

        verifyNoInteractions(usuarioService);
    }

    @Test
    void loginDevuelveMensajeGenericoParaCredencialesInvalidas() throws Exception {
        when(usuarioService.login(anyString(), anyString()))
                .thenThrow(new BadCredentialsException("Usuario no encontrado: dato interno"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"ana@vet.com\",\"clave\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Credenciales inválidas"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("dato interno"))));
    }

    @Test
    void loginCorrectoNormalizaCorreoGeneraJwtYNoExponeClave() throws Exception {
        Usuario usuario = usuario();
        when(usuarioService.login("ana@vet.com", "ClaveSegura123")).thenReturn(usuario);
        when(jwtUtil.generarToken(usuario)).thenReturn("jwt-esperado");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"  ANA@VET.COM  \",\"clave\":\"ClaveSegura123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-esperado"))
                .andExpect(jsonPath("$.usuario.correo").value("ana@vet.com"))
                .andExpect(jsonPath("$.usuario.clave").doesNotExist());
    }

    @Test
    void errorInesperadoNoExponeDetalleInterno() throws Exception {
        when(usuarioService.login(anyString(), anyString()))
                .thenThrow(new RuntimeException("SQL secreto=password-privada"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"ana@vet.com\",\"clave\":\"ClaveSegura123\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Error interno del servidor"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("password-privada"))));
    }

    private String bearerAdmin() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("admin@vet.com");
        when(claims.get("rol", String.class)).thenReturn("ADMIN");
        when(jwtUtil.validar("token-admin")).thenReturn(claims);
        return "Bearer token-admin";
    }

    private String registroJson(String correo, String clave, String rol) {
        return "{\"nombre\":\"Ana\",\"apellido\":\"López\",\"correo\":" + correo
                + ",\"clave\":" + clave + ",\"rol\":" + rol + "}";
    }

    private Usuario usuario() {
        Rol rol = new Rol();
        rol.setId(1);
        rol.setNombre("ADMIN");
        Usuario usuario = new Usuario();
        usuario.setId(10);
        usuario.setNombre("Ana");
        usuario.setApellido("López");
        usuario.setCorreo("ana@vet.com");
        usuario.setClave("hash-no-expuesto");
        usuario.setActivo(true);
        usuario.setRol(rol);
        return usuario;
    }
}
