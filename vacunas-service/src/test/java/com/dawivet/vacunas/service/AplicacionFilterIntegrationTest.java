package com.dawivet.vacunas.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.dawivet.vacunas.client.MascotaClient;
import com.dawivet.vacunas.client.VeterinarioClient;
import com.dawivet.vacunas.model.AplicacionVacuna;
import com.dawivet.vacunas.model.EstadoAplicacion;
import com.dawivet.vacunas.repository.AplicacionVacunaRepository;

@SpringBootTest
@Transactional
class AplicacionFilterIntegrationTest {

    @Autowired
    private AplicacionVacunaRepository repository;

    @Autowired
    private AplicacionVacunaService service;

    @MockitoBean
    private MascotaClient mascotaClient;

    @MockitoBean
    private VeterinarioClient veterinarioClient;

    @BeforeEach
    void seed() {
        repository.deleteAll();
        repository.save(application("Firulais", 2, 3, 1,
                EstadoAplicacion.REGISTRADA, LocalDate.of(2026, 9, 1)));
        repository.save(application("FIRULA", 4, 3, 1,
                EstadoAplicacion.ANULADA, LocalDate.of(2026, 9, 3)));
        repository.save(application("Luna", 2, 8, 5,
                EstadoAplicacion.REGISTRADA, LocalDate.of(2026, 8, 1)));
        repository.flush();
    }

    @Test
    void filtraParcialmenteNombreHistoricoSinDistinguirMayusculas() {
        var result = service.buscar(0, 10, "rula", null, null, null,
                null, null, null, "fechaAplicacion", "desc");

        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.content()).extracting(value -> value.mascotaNombre().toLowerCase())
                .allMatch(name -> name.contains("rula"));
    }

    @Test
    void combinaIdsEstadoYRangoDeFechas() {
        var result = service.buscar(0, 10, null, 2, 3, 1,
                EstadoAplicacion.REGISTRADA, LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30), "fechaAplicacion", "desc");

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).mascotaNombre()).isEqualTo("Firulais");
    }

    @Test
    void filtroSinCoincidenciasEntregaPaginaVacia() {
        var result = service.buscar(0, 10, "NoExiste", null, null, null,
                null, null, null, null, null);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    private AplicacionVacuna application(String name, int petId, int vetId, int vaccineId,
            EstadoAplicacion state, LocalDate date) {
        AplicacionVacuna value = new AplicacionVacuna();
        value.setMascotaNombre(name);
        value.setMascotaId(petId);
        value.setVeterinarioId(vetId);
        value.setVacunaId(vaccineId);
        value.setVeterinarioNombre("Veterinario " + vetId);
        value.setVacunaNombre("Vacuna " + vaccineId);
        value.setFechaAplicacion(date);
        value.setEstado(state);
        return value;
    }
}
