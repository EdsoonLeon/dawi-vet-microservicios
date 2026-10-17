package com.dawivet.vacunas.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.dawivet.vacunas.client.MascotaClient;
import com.dawivet.vacunas.client.VeterinarioClient;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAndErrorIntegrationTest {

    private static final String SECRET = "dawivet-clave-secreta-para-jwt-2026-minimo-32";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MascotaClient mascotaClient;

    @MockitoBean
    private VeterinarioClient veterinarioClient;

    @Test
    void tokenAusenteRetorna401EnJson() throws Exception {
        mockMvc.perform(get("/api/vacunas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTENTICACION_REQUERIDA"))
                .andExpect(jsonPath("$.error.details").isArray())
                .andExpect(jsonPath("$.path").value("/api/vacunas"));
    }

    @Test
    void tokenInvalidoRetorna401EnJson() throws Exception {
        mockMvc.perform(get("/api/vacunas").header("Authorization", "Bearer no-es-un-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("TOKEN_INVALIDO"));
    }

    @Test
    void recepcionistaNoPuedeRegistrarVacuna() throws Exception {
        mockMvc.perform(post("/api/vacunas/registrar")
                        .header("Authorization", bearer("RECEPCIONISTA"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Rabia","descripcion":"Anual","enfermedadPreviene":"Rabia"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ACCESO_DENEGADO"));
    }

    @Test
    void adminRegistraVacunaYRecibeContratoDeExito() throws Exception {
        mockMvc.perform(post("/api/vacunas/registrar")
                        .header("Authorization", bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Parvovirus","descripcion":"Primera dosis",
                                 "enfermedadPreviene":"Parvovirosis"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.activo").value(true))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void validacionDeCamposTieneFormatoUniformeYNoAceptaActivoComoDatoDeDominio() throws Exception {
        mockMvc.perform(post("/api/vacunas/registrar")
                        .header("Authorization", bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":" ","enfermedadPreviene":"","activo":false}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("JSON_INVALIDO"))
                .andExpect(jsonPath("$.error.details").isArray())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void jsonMalformadoTieneFormatoUniforme() throws Exception {
        mockMvc.perform(post("/api/vacunas/registrar")
                        .header("Authorization", bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("JSON_INVALIDO"))
                .andExpect(jsonPath("$.error.details").isArray());
    }

    @Test
    void camposInvalidosIncluyenDetalleDeCampo() throws Exception {
        mockMvc.perform(post("/api/vacunas/registrar")
                        .header("Authorization", bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":" ","enfermedadPreviene":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDACION_CAMPOS"))
                .andExpect(jsonPath("$.error.details[0].field").exists())
                .andExpect(jsonPath("$.error.details[0].message").exists());
    }

    @Test
    void rolDeLecturaPuedeConsultarPaginaVacia() throws Exception {
        mockMvc.perform(get("/api/vacunas")
                        .header("Authorization", bearer("VETERINARIO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.totalElements").isNumber());
    }

    private String bearer(String role) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("pruebas@dawivet.com")
                .claim("id", 1)
                .claim("rol", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();
        return "Bearer " + token;
    }
}
