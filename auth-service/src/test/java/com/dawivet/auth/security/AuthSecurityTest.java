package com.dawivet.auth.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.dawivet.auth.controller.AuthController;
import com.dawivet.auth.controller.RolController;
import com.dawivet.auth.controller.UsuarioController;
import com.dawivet.auth.dto.UsuarioRegistroRequest;
import com.dawivet.auth.model.Rol;
import com.dawivet.auth.model.Usuario;
import com.dawivet.auth.service.RolService;
import com.dawivet.auth.service.UsuarioService;

import io.jsonwebtoken.Claims;

@WebMvcTest({AuthController.class, UsuarioController.class, RolController.class})
@Import({SecurityConfig.class, JwtFilter.class})
class AuthSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private RolService rolService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void registroSinTokenRetorna401() throws Exception {
        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void registroConTokenInvalidoRetorna401() throws Exception {
        when(jwtUtil.validar("invalido")).thenThrow(new IllegalArgumentException("Token inválido"));

        mockMvc.perform(post("/api/auth/registrar")
                        .header("Authorization", "Bearer invalido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registroConTokenExpiradoRetorna401() throws Exception {
        when(jwtUtil.validar("expirado")).thenThrow(new IllegalArgumentException("Token expirado"));

        mockMvc.perform(post("/api/auth/registrar")
                        .header("Authorization", "Bearer expirado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void veterinarioNoPuedeRegistrarUsuarios() throws Exception {
        mockMvc.perform(post("/api/auth/registrar")
                        .header("Authorization", bearer("VETERINARIO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void recepcionistaNoPuedeRegistrarUsuarios() throws Exception {
        mockMvc.perform(post("/api/auth/registrar")
                        .header("Authorization", bearer("RECEPCIONISTA"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void adminPuedeRegistrarSinExponerClaveNiAceptarCamposInternos() throws Exception {
        when(usuarioService.registrarUsuario(any())).thenReturn(usuarioCreado());

        mockMvc.perform(post("/api/auth/registrar")
                        .header("Authorization", bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 999,
                                  "nombre": "Ana",
                                  "apellido": "López",
                                  "correo": "ana@vet.com",
                                  "clave": "ClaveSegura123",
                                  "activo": false,
                                  "rol": {"id": 3, "nombre": "IGNORADO"}
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.clave").doesNotExist());

        ArgumentCaptor<UsuarioRegistroRequest> captor = ArgumentCaptor.forClass(UsuarioRegistroRequest.class);
        verify(usuarioService).registrarUsuario(captor.capture());
        UsuarioRegistroRequest request = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals("ana@vet.com", request.correo());
        org.junit.jupiter.api.Assertions.assertEquals(3, request.rol().id());
    }

    @Test
    void usuarioNoAdminNoPuedeConsultarAdministracion() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", bearer("VETERINARIO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPuedeConsultarAdministracion() throws Exception {
        when(usuarioService.listarUsuariosActivos()).thenReturn(List.of());

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", bearer("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void rolesContinuaSiendoPublico() throws Exception {
        when(rolService.listarRoles()).thenReturn(List.of(rol(3, "VETERINARIO")));

        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].nombre").value("VETERINARIO"));
    }

    @Test
    void noExisteEndpointHttpDeBootstrap() throws Exception {
        mockMvc.perform(post("/api/auth/bootstrap")
                        .header("Authorization", bearer("ADMIN")))
                .andExpect(status().isNotFound());
    }

    private String bearer(String role) {
        String token = "token-" + role;
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("admin@vet.com");
        when(claims.get("rol", String.class)).thenReturn(role);
        when(jwtUtil.validar(token)).thenReturn(claims);
        return "Bearer " + token;
    }

    private String registroJson() {
        return """
                {"nombre":"Ana","apellido":"López","correo":"ana@vet.com",
                 "clave":"ClaveSegura123","rol":{"id":3}}
                """;
    }

    private Usuario usuarioCreado() {
        Usuario usuario = new Usuario();
        usuario.setId(10);
        usuario.setNombre("Ana");
        usuario.setApellido("López");
        usuario.setCorreo("ana@vet.com");
        usuario.setClave("hash-no-expuesto");
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
