package com.dawivet.auth.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.dawivet.auth.dto.UsuarioActualizacionRequest;
import com.dawivet.auth.exception.ApiExceptionHandler;
import com.dawivet.auth.model.Rol;
import com.dawivet.auth.model.Usuario;
import com.dawivet.auth.security.JwtFilter;
import com.dawivet.auth.security.JwtUtil;
import com.dawivet.auth.security.SecurityConfig;
import com.dawivet.auth.service.UsuarioService;

import io.jsonwebtoken.Claims;

@WebMvcTest(UsuarioController.class)
@Import({SecurityConfig.class, JwtFilter.class, ApiExceptionHandler.class})
class UsuarioValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void identificadoresCeroONegativosSonRechazados() throws Exception {
        mockMvc.perform(get("/api/usuarios/0").header("Authorization", bearerAdmin()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/usuarios/-1")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/usuarios/0/activar").header("Authorization", bearerAdmin()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(delete("/api/usuarios/-2").header("Authorization", bearerAdmin()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void actualizacionRechazaCorreoInvalidoOVacio() throws Exception {
        String[] correos = {"correo-invalido", "   "};

        for (String correo : correos) {
            mockMvc.perform(put("/api/usuarios/10")
                            .header("Authorization", bearerAdmin())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"correo\":\"" + correo + "\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Datos de entrada inválidos"));
        }

        verifyNoInteractions(usuarioService);
    }

    @Test
    void actualizacionRechazaRolConIdNoPositivo() throws Exception {
        mockMvc.perform(put("/api/usuarios/10")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rol\":{\"id\":0}}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void actualizacionNoPermiteControlarIdNiActivo() throws Exception {
        Usuario existente = usuario();
        when(usuarioService.editarUsuario(eq(10), any())).thenReturn(existente);

        mockMvc.perform(put("/api/usuarios/10")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 999,
                                  "activo": false,
                                  "nombre": "Ana María",
                                  "correo": "  NUEVO@VET.COM  "
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.clave").doesNotExist());

        ArgumentCaptor<UsuarioActualizacionRequest> captor = ArgumentCaptor.forClass(UsuarioActualizacionRequest.class);
        verify(usuarioService).editarUsuario(eq(10), captor.capture());
        assertEquals("nuevo@vet.com", captor.getValue().correo());
    }

    @Test
    void erroresDeActualizacionConservanCodigosEsperados() throws Exception {
        when(usuarioService.editarUsuario(eq(10), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado"))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no encontrado"));

        mockMvc.perform(put("/api/usuarios/10")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/usuarios/10")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict());
        mockMvc.perform(put("/api/usuarios/10")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    private String bearerAdmin() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("admin@vet.com");
        when(claims.get("rol", String.class)).thenReturn("ADMIN");
        when(jwtUtil.validar("token-admin")).thenReturn(claims);
        return "Bearer token-admin";
    }

    private Usuario usuario() {
        Rol rol = new Rol();
        rol.setId(1);
        rol.setNombre("ADMIN");
        Usuario usuario = new Usuario();
        usuario.setId(10);
        usuario.setNombre("Ana");
        usuario.setApellido("López");
        usuario.setCorreo("nuevo@vet.com");
        usuario.setClave("hash-no-expuesto");
        usuario.setActivo(true);
        usuario.setRol(rol);
        return usuario;
    }
}
