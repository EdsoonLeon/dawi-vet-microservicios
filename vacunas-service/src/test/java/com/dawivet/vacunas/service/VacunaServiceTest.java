package com.dawivet.vacunas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.dawivet.vacunas.dto.VacunaRequest;
import com.dawivet.vacunas.exception.BadRequestException;
import com.dawivet.vacunas.exception.ConflictException;
import com.dawivet.vacunas.model.Vacuna;
import com.dawivet.vacunas.repository.VacunaRepository;

@ExtendWith(MockitoExtension.class)
class VacunaServiceTest {

    @Mock
    private VacunaRepository repository;

    @InjectMocks
    private VacunaService service;

    @Test
    void registraVacunaNormalizandoLosCampos() {
        when(repository.existsByNombreIgnoreCase("Rabia")).thenReturn(false);
        when(repository.save(any(Vacuna.class))).thenAnswer(invocation -> {
            Vacuna value = invocation.getArgument(0);
            value.setId(1);
            return value;
        });

        var result = service.registrar(new VacunaRequest("  Rabia  ", "  Anual  ", "  Rabia  "));

        assertThat(result.id()).isEqualTo(1);
        assertThat(result.nombre()).isEqualTo("Rabia");
        assertThat(result.activo()).isTrue();
    }

    @Test
    void rechazaVacunaDuplicadaSinDistinguirMayusculas() {
        when(repository.existsByNombreIgnoreCase("rabia")).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(new VacunaRequest(" rabia ", null, "Rabia")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("mismo nombre");
    }

    @Test
    @SuppressWarnings("unchecked")
    void paginaCatalogoEnBaseDeDatosConOrdenEstable() {
        Vacuna vacuna = vacuna(2, "Rabia", true);
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(vacuna)));

        var result = service.buscar(0, 10, "rab", true, "nombre", "asc");

        assertThat(result.content()).hasSize(1);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), pageable.capture());
        assertThat(pageable.getValue().getSort().getOrderFor("id")).isNotNull();
    }

    @Test
    void rechazaPaginacionInvalida() {
        assertThatThrownBy(() -> service.buscar(-1, 101, null, null, "nombre", "asc"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void desactivacionRepetidaEsConflicto() {
        when(repository.findById(1)).thenReturn(Optional.of(vacuna(1, "Rabia", false)));

        assertThatThrownBy(() -> service.desactivar(1))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("inactiva");
    }

    private Vacuna vacuna(int id, String nombre, boolean activo) {
        Vacuna vacuna = new Vacuna();
        vacuna.setId(id);
        vacuna.setNombre(nombre);
        vacuna.setEnfermedadPreviene("Enfermedad");
        vacuna.setActivo(activo);
        return vacuna;
    }
}
