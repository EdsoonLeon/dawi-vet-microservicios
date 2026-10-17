package com.dawivet.veterinarios.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.dawivet.veterinarios.controller.VeterinarioController;
import com.dawivet.veterinarios.model.Veterinario;
import com.dawivet.veterinarios.service.VeterinarioService;

import io.jsonwebtoken.Claims;

@WebMvcTest(VeterinarioController.class)
@Import({SecurityConfig.class, JwtFilter.class})
class VeterinarioSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VeterinarioService service;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void registroSinTokenRetorna401() throws Exception {
        mockMvc.perform(post("/api/veterinarios/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    void veterinarioNoPuedeCrearOtroVeterinario() throws Exception {
        mockMvc.perform(post("/api/veterinarios/registrar")
                        .header("Authorization", bearer("VETERINARIO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    void adminPuedeIniciarElFlujoDeRegistro() throws Exception {
        Veterinario veterinario = new Veterinario();
        veterinario.setId(7);
        veterinario.setUsuarioId(20);
        veterinario.setActivo(true);
        when(service.registrarVeterinario(any())).thenReturn(veterinario);

        mockMvc.perform(post("/api/veterinarios/registrar")
                        .header("Authorization", bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.usuarioId").value(20));
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
                {
                  "especialidad":"Cirugía",
                  "colegiatura":"CMVP-1234",
                  "edad":35,
                  "usuario":{
                    "nombre":"Ana","apellido":"López",
                    "correo":"ana@vet.com","clave":"ClaveSegura123"
                  }
                }
                """;
    }
}
