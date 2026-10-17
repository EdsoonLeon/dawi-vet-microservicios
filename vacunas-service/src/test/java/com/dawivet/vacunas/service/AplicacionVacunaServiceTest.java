package com.dawivet.vacunas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.dawivet.vacunas.client.MascotaClient;
import com.dawivet.vacunas.client.VeterinarioClient;
import com.dawivet.vacunas.dto.AplicacionVacunaRequest;
import com.dawivet.vacunas.dto.AplicacionVacunaUpdateRequest;
import com.dawivet.vacunas.dto.MascotaDto;
import com.dawivet.vacunas.dto.VeterinarioDto;
import com.dawivet.vacunas.exception.BadRequestException;
import com.dawivet.vacunas.exception.BusinessRuleException;
import com.dawivet.vacunas.exception.ConflictException;
import com.dawivet.vacunas.exception.ExternalServiceException;
import com.dawivet.vacunas.exception.ResourceNotFoundException;
import com.dawivet.vacunas.model.AplicacionVacuna;
import com.dawivet.vacunas.model.EstadoAplicacion;
import com.dawivet.vacunas.model.Vacuna;
import com.dawivet.vacunas.repository.AplicacionVacunaRepository;

import feign.FeignException;
import feign.RetryableException;

@ExtendWith(MockitoExtension.class)
class AplicacionVacunaServiceTest {

    @Mock
    private AplicacionVacunaRepository repository;
    @Mock
    private VacunaService vacunaService;
    @Mock
    private MascotaClient mascotaClient;
    @Mock
    private VeterinarioClient veterinarioClient;

    @InjectMocks
    private AplicacionVacunaService service;

    @Test
    void registraAplicacionSoloDespuesDeValidarDependencias() {
        validDependencies();
        when(repository.save(any(AplicacionVacuna.class))).thenAnswer(invocation -> {
            AplicacionVacuna value = invocation.getArgument(0);
            value.setId(10);
            return value;
        });

        var result = service.registrar(validRequest());

        assertThat(result.id()).isEqualTo(10);
        assertThat(result.mascotaNombre()).isEqualTo("Firulais");
        assertThat(result.veterinarioNombre()).isEqualTo("Ana Pérez");
        assertThat(result.vacunaNombre()).isEqualTo("Rabia");
        assertThat(result.estado()).isEqualTo(EstadoAplicacion.REGISTRADA);
    }

    @Test
    void mascotaInexistenteDevuelveCodigoEspecifico() {
        when(vacunaService.find(1)).thenReturn(vaccine(true));
        when(mascotaClient.obtenerPorId(2)).thenThrow(org.mockito.Mockito.mock(FeignException.NotFound.class));

        assertCode(() -> service.registrar(validRequest()), ResourceNotFoundException.class,
                "MASCOTA_NO_ENCONTRADA");
        verify(repository, never()).save(any());
    }

    @Test
    void mascotaInactivaEsReglaSemantica() {
        when(vacunaService.find(1)).thenReturn(vaccine(true));
        when(mascotaClient.obtenerPorId(2)).thenReturn(new MascotaDto(2, "Firulais", false));

        assertCode(() -> service.registrar(validRequest()), BusinessRuleException.class, "MASCOTA_INACTIVA");
    }

    @Test
    void veterinarioInexistenteDevuelveCodigoEspecifico() {
        when(vacunaService.find(1)).thenReturn(vaccine(true));
        when(mascotaClient.obtenerPorId(2)).thenReturn(new MascotaDto(2, "Firulais", true));
        when(veterinarioClient.obtenerPorId(3))
                .thenThrow(org.mockito.Mockito.mock(FeignException.NotFound.class));

        assertCode(() -> service.registrar(validRequest()), ResourceNotFoundException.class,
                "VETERINARIO_NO_ENCONTRADO");
    }

    @Test
    void veterinarioInactivoEsReglaSemantica() {
        when(vacunaService.find(1)).thenReturn(vaccine(true));
        when(mascotaClient.obtenerPorId(2)).thenReturn(new MascotaDto(2, "Firulais", true));
        when(veterinarioClient.obtenerPorId(3)).thenReturn(new VeterinarioDto(3, "Ana", "Pérez", false));

        assertCode(() -> service.registrar(validRequest()), BusinessRuleException.class,
                "VETERINARIO_INACTIVO");
    }

    @Test
    void vacunaInexistenteSePropagaSinLlamarFeign() {
        when(vacunaService.find(1)).thenThrow(
                new ResourceNotFoundException("VACUNA_NO_ENCONTRADA", "No encontrada"));

        assertCode(() -> service.registrar(validRequest()), ResourceNotFoundException.class,
                "VACUNA_NO_ENCONTRADA");
        verify(mascotaClient, never()).obtenerPorId(any());
    }

    @Test
    void vacunaInactivaImpideElRegistro() {
        when(vacunaService.find(1)).thenReturn(vaccine(false));

        assertCode(() -> service.registrar(validRequest()), BusinessRuleException.class, "VACUNA_INACTIVA");
        verify(mascotaClient, never()).obtenerPorId(any());
    }

    @Test
    void respuestaFeignNulaOInconsistenteEs502() {
        when(vacunaService.find(1)).thenReturn(vaccine(true));
        when(mascotaClient.obtenerPorId(2)).thenReturn(new MascotaDto(99, "Firulais", true));

        assertCode(() -> service.registrar(validRequest()), ExternalServiceException.class,
                "RESPUESTA_SERVICIO_INVALIDA");
    }

    @Test
    void servicioExternoNoDisponibleEs503() {
        when(vacunaService.find(1)).thenReturn(vaccine(true));
        RetryableException retryable = org.mockito.Mockito.mock(RetryableException.class);
        when(retryable.getCause()).thenReturn(new ConnectException("connection refused"));
        when(mascotaClient.obtenerPorId(2)).thenThrow(retryable);

        assertCode(() -> service.registrar(validRequest()), ExternalServiceException.class,
                "SERVICIO_EXTERNO_NO_DISPONIBLE");
    }

    @Test
    void eurekaSinInstanciasRepresentadoComo503EsNoDisponible() {
        when(vacunaService.find(1)).thenReturn(vaccine(true));
        FeignException exception = org.mockito.Mockito.mock(FeignException.class);
        when(exception.status()).thenReturn(503);
        when(mascotaClient.obtenerPorId(2)).thenThrow(exception);

        assertCode(() -> service.registrar(validRequest()), ExternalServiceException.class,
                "SERVICIO_EXTERNO_NO_DISPONIBLE");
    }

    @Test
    void timeoutExternoEs504() {
        when(vacunaService.find(1)).thenReturn(vaccine(true));
        RetryableException retryable = org.mockito.Mockito.mock(RetryableException.class);
        when(retryable.getCause()).thenReturn(new SocketTimeoutException("read timed out"));
        when(mascotaClient.obtenerPorId(2)).thenThrow(retryable);

        assertCode(() -> service.registrar(validRequest()), ExternalServiceException.class,
                "SERVICIO_EXTERNO_TIMEOUT");
    }

    @Test
    void rechazaProximaDosisNoPosterior() {
        validDependencies();
        AplicacionVacunaRequest request = new AplicacionVacunaRequest(
                1, 2, 3, LocalDate.now(), LocalDate.now(), null, null);

        assertCode(() -> service.registrar(request), BusinessRuleException.class,
                "PROXIMA_DOSIS_INVALIDA");
        verify(repository, never()).save(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void filtroParcialPorNombreUsaConsultaPaginada() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(application(1, EstadoAplicacion.REGISTRADA))));

        var page = service.buscar(0, 10, "rulA", null, null, null,
                null, null, null, "fechaAplicacion", "desc");

        assertThat(page.content()).hasSize(1);
        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void admiteFiltrosCombinados() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(application(1, EstadoAplicacion.REGISTRADA))));

        var page = service.buscar(0, 10, "Fir", 2, 3, 1, EstadoAplicacion.REGISTRADA,
                LocalDate.now().minusDays(2), LocalDate.now(), "id", "asc");

        assertThat(page.totalElements()).isEqualTo(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void sinCoincidenciasRetornaPaginaVacia() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertThat(service.buscar(0, 10, "NoExiste", null, null, null,
                null, null, null, null, null).content()).isEmpty();
    }

    @Test
    void rechazaRangoYPaginacionInvalidos() {
        assertThatThrownBy(() -> service.buscar(0, 0, null, null, null, null,
                null, null, null, null, null)).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.buscar(0, 10, null, null, null, null, null,
                LocalDate.now(), LocalDate.now().minusDays(1), null, null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void corrigeYAnulaAplicacionRegistrada() {
        AplicacionVacuna application = application(1, EstadoAplicacion.REGISTRADA);
        when(repository.findById(1)).thenReturn(Optional.of(application));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var corrected = service.actualizar(1, new AplicacionVacunaUpdateRequest(
                LocalDate.now().minusDays(2), LocalDate.now().plusDays(20), " L-2 ", " control "));
        var cancelled = service.anular(1, " registro duplicado ");

        assertThat(corrected.lote()).isEqualTo("L-2");
        assertThat(cancelled.estado()).isEqualTo(EstadoAplicacion.ANULADA);
        assertThat(cancelled.motivoAnulacion()).isEqualTo("registro duplicado");
        assertThat(cancelled.fechaAnulacion()).isNotNull();
    }

    @Test
    void noPermiteModificarAplicacionAnulada() {
        when(repository.findById(1)).thenReturn(Optional.of(application(1, EstadoAplicacion.ANULADA)));

        assertThatThrownBy(() -> service.actualizar(1, new AplicacionVacunaUpdateRequest(
                LocalDate.now(), null, null, null))).isInstanceOf(ConflictException.class);
    }

    private void validDependencies() {
        when(vacunaService.find(1)).thenReturn(vaccine(true));
        when(mascotaClient.obtenerPorId(2)).thenReturn(new MascotaDto(2, " Firulais ", true));
        when(veterinarioClient.obtenerPorId(3)).thenReturn(new VeterinarioDto(3, " Ana ", " Pérez ", true));
    }

    private AplicacionVacunaRequest validRequest() {
        return new AplicacionVacunaRequest(1, 2, 3,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30), "L-1", "Sin reacción");
    }

    private Vacuna vaccine(boolean active) {
        Vacuna vacuna = new Vacuna();
        vacuna.setId(1);
        vacuna.setNombre("Rabia");
        vacuna.setEnfermedadPreviene("Rabia");
        vacuna.setActivo(active);
        return vacuna;
    }

    private AplicacionVacuna application(int id, EstadoAplicacion state) {
        AplicacionVacuna value = new AplicacionVacuna();
        value.setId(id);
        value.setVacunaId(1);
        value.setMascotaId(2);
        value.setVeterinarioId(3);
        value.setFechaAplicacion(LocalDate.now().minusDays(1));
        value.setMascotaNombre("Firulais");
        value.setVeterinarioNombre("Ana Pérez");
        value.setVacunaNombre("Rabia");
        value.setEstado(state);
        return value;
    }

    private <T extends com.dawivet.vacunas.exception.DomainException> void assertCode(
            org.assertj.core.api.ThrowableAssert.ThrowingCallable callable,
            Class<T> type, String code) {
        assertThatThrownBy(callable).isInstanceOf(type)
                .satisfies(ex -> assertThat(((T) ex).getCode()).isEqualTo(code));
    }
}
