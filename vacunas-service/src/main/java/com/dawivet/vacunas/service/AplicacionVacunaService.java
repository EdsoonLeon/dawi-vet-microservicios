package com.dawivet.vacunas.service;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dawivet.vacunas.client.MascotaClient;
import com.dawivet.vacunas.client.VeterinarioClient;
import com.dawivet.vacunas.dto.AplicacionVacunaRequest;
import com.dawivet.vacunas.dto.AplicacionVacunaResponse;
import com.dawivet.vacunas.dto.AplicacionVacunaUpdateRequest;
import com.dawivet.vacunas.dto.MascotaDto;
import com.dawivet.vacunas.dto.PageResponse;
import com.dawivet.vacunas.dto.VeterinarioDto;
import com.dawivet.vacunas.exception.BadRequestException;
import com.dawivet.vacunas.exception.BusinessRuleException;
import com.dawivet.vacunas.exception.ConflictException;
import com.dawivet.vacunas.exception.ExternalServiceException;
import com.dawivet.vacunas.exception.MissingAuthenticationContextException;
import com.dawivet.vacunas.exception.ResourceNotFoundException;
import com.dawivet.vacunas.model.AplicacionVacuna;
import com.dawivet.vacunas.model.EstadoAplicacion;
import com.dawivet.vacunas.model.Vacuna;
import com.dawivet.vacunas.repository.AplicacionVacunaRepository;

import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AplicacionVacunaService {

    private static final Set<String> SORT_FIELDS = Set.of(
            "id", "fechaAplicacion", "proximaDosis", "mascotaNombre",
            "vacunaNombre", "veterinarioNombre", "estado");

    private final AplicacionVacunaRepository repository;
    private final VacunaService vacunaService;
    private final MascotaClient mascotaClient;
    private final VeterinarioClient veterinarioClient;

    @Transactional
    public AplicacionVacunaResponse registrar(AplicacionVacunaRequest request) {
        Vacuna vacuna = vacunaService.find(request.vacunaId());
        if (!Boolean.TRUE.equals(vacuna.getActivo())) {
            throw new BusinessRuleException("VACUNA_INACTIVA", "La vacuna seleccionada se encuentra inactiva");
        }

        MascotaDto mascota = obtenerMascota(request.mascotaId());
        validarMascota(mascota, request.mascotaId());
        VeterinarioDto veterinario = obtenerVeterinario(request.veterinarioId());
        validarVeterinario(veterinario, request.veterinarioId());
        validateDates(request.fechaAplicacion(), request.proximaDosis());

        AplicacionVacuna aplicacion = new AplicacionVacuna();
        aplicacion.setVacunaId(vacuna.getId());
        aplicacion.setMascotaId(mascota.id());
        aplicacion.setVeterinarioId(veterinario.id());
        aplicacion.setFechaAplicacion(request.fechaAplicacion());
        aplicacion.setProximaDosis(request.proximaDosis());
        aplicacion.setLote(normalizeOptional(request.lote()));
        aplicacion.setObservacion(normalizeOptional(request.observacion()));
        aplicacion.setMascotaNombre(mascota.nombre().trim());
        aplicacion.setVeterinarioNombre(fullName(veterinario));
        aplicacion.setVacunaNombre(vacuna.getNombre());
        aplicacion.setEstado(EstadoAplicacion.REGISTRADA);
        return toResponse(repository.save(aplicacion));
    }

    @Transactional(readOnly = true)
    public PageResponse<AplicacionVacunaResponse> buscar(
            int page, int size, String nombreMascota, Integer mascotaId, Integer veterinarioId,
            Integer vacunaId, EstadoAplicacion estado, LocalDate fechaDesde, LocalDate fechaHasta,
            String sortBy, String direction) {
        validateFilters(page, size, nombreMascota, mascotaId, veterinarioId, vacunaId, fechaDesde, fechaHasta);
        Pageable pageable = pageable(page, size, sortBy, direction);
        Specification<AplicacionVacuna> specification = (root, query, cb) -> cb.conjunction();

        if (nombreMascota != null) {
            String filter = nombreMascota.trim().toLowerCase(Locale.ROOT);
            specification = specification.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("mascotaNombre")), "%" + filter + "%"));
        }
        if (mascotaId != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("mascotaId"), mascotaId));
        }
        if (veterinarioId != null) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.get("veterinarioId"), veterinarioId));
        }
        if (vacunaId != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("vacunaId"), vacunaId));
        }
        if (estado != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("estado"), estado));
        }
        if (fechaDesde != null) {
            specification = specification.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("fechaAplicacion"), fechaDesde));
        }
        if (fechaHasta != null) {
            specification = specification.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("fechaAplicacion"), fechaHasta));
        }

        Page<AplicacionVacuna> result = repository.findAll(specification, pageable);
        return PageResponse.from(result, result.getContent().stream().map(this::toResponse).toList());
    }

    @Transactional(readOnly = true)
    public AplicacionVacunaResponse obtener(Integer id) {
        return toResponse(find(id));
    }

    @Transactional
    public AplicacionVacunaResponse actualizar(Integer id, AplicacionVacunaUpdateRequest request) {
        AplicacionVacuna aplicacion = find(id);
        ensureRegistered(aplicacion);
        validateDates(request.fechaAplicacion(), request.proximaDosis());
        aplicacion.setFechaAplicacion(request.fechaAplicacion());
        aplicacion.setProximaDosis(request.proximaDosis());
        aplicacion.setLote(normalizeOptional(request.lote()));
        aplicacion.setObservacion(normalizeOptional(request.observacion()));
        return toResponse(repository.save(aplicacion));
    }

    @Transactional
    public AplicacionVacunaResponse anular(Integer id, String motivo) {
        AplicacionVacuna aplicacion = find(id);
        ensureRegistered(aplicacion);
        aplicacion.setEstado(EstadoAplicacion.ANULADA);
        aplicacion.setMotivoAnulacion(motivo.trim());
        aplicacion.setFechaAnulacion(LocalDateTime.now());
        return toResponse(repository.save(aplicacion));
    }

    private AplicacionVacuna find(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "APLICACION_NO_ENCONTRADA", "No se encontró la aplicación de vacuna solicitada"));
    }

    private void ensureRegistered(AplicacionVacuna aplicacion) {
        if (aplicacion.getEstado() == EstadoAplicacion.ANULADA) {
            throw new ConflictException(
                    "APLICACION_ANULADA", "Una aplicación anulada no puede modificarse ni anularse nuevamente");
        }
    }

    private MascotaDto obtenerMascota(Integer id) {
        try {
            return mascotaClient.obtenerPorId(id);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("MASCOTA_NO_ENCONTRADA", "No se encontró la mascota indicada");
        } catch (RetryableException ex) {
            throw availability(ex, "mascotas-service");
        } catch (FeignException ex) {
            throw feignFailure(ex, "mascotas-service");
        } catch (MissingAuthenticationContextException ex) {
            throw missingAuthenticationContext();
        }
    }

    private VeterinarioDto obtenerVeterinario(Integer id) {
        try {
            return veterinarioClient.obtenerPorId(id);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException(
                    "VETERINARIO_NO_ENCONTRADO", "No se encontró el veterinario indicado");
        } catch (RetryableException ex) {
            throw availability(ex, "veterinarios-service");
        } catch (FeignException ex) {
            throw feignFailure(ex, "veterinarios-service");
        } catch (MissingAuthenticationContextException ex) {
            throw missingAuthenticationContext();
        }
    }

    private void validarMascota(MascotaDto mascota, Integer requestedId) {
        if (mascota == null || !requestedId.equals(mascota.id())
                || mascota.nombre() == null || mascota.nombre().isBlank()
                || mascota.nombre().trim().length() > 120 || mascota.activo() == null) {
            throw invalidResponse("mascotas-service");
        }
        if (!mascota.activo()) {
            throw new BusinessRuleException("MASCOTA_INACTIVA", "La mascota se encuentra inactiva");
        }
    }

    private void validarVeterinario(VeterinarioDto veterinario, Integer requestedId) {
        if (veterinario == null || !requestedId.equals(veterinario.id())
                || veterinario.nombre() == null || veterinario.nombre().isBlank()
                || veterinario.apellido() == null || veterinario.apellido().isBlank()
                || fullName(veterinario).length() > 241
                || veterinario.activo() == null) {
            throw invalidResponse("veterinarios-service");
        }
        if (!veterinario.activo()) {
            throw new BusinessRuleException("VETERINARIO_INACTIVO", "El veterinario se encuentra inactivo");
        }
    }

    private void validateDates(LocalDate fechaAplicacion, LocalDate proximaDosis) {
        if (fechaAplicacion.isAfter(LocalDate.now())) {
            throw new BusinessRuleException(
                    "FECHA_APLICACION_INVALIDA", "La fecha de aplicación no puede ser futura");
        }
        if (proximaDosis != null && !proximaDosis.isAfter(fechaAplicacion)) {
            throw new BusinessRuleException(
                    "PROXIMA_DOSIS_INVALIDA", "La próxima dosis debe ser posterior a la fecha de aplicación");
        }
    }

    private void validateFilters(int page, int size, String nombreMascota,
            Integer mascotaId, Integer veterinarioId, Integer vacunaId,
            LocalDate fechaDesde, LocalDate fechaHasta) {
        VacunaService.validatePage(page, size);
        if (nombreMascota != null && nombreMascota.isBlank()) {
            throw new BadRequestException(
                    "FILTRO_INVALIDO", "El filtro nombreMascota no puede contener solo espacios");
        }
        if ((mascotaId != null && mascotaId <= 0)
                || (veterinarioId != null && veterinarioId <= 0)
                || (vacunaId != null && vacunaId <= 0)) {
            throw new BadRequestException("FILTRO_INVALIDO", "Los identificadores de filtro deben ser positivos");
        }
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new BadRequestException(
                    "RANGO_FECHAS_INVALIDO", "fechaDesde no puede ser posterior a fechaHasta");
        }
    }

    private Pageable pageable(int page, int size, String sortBy, String direction) {
        String field = sortBy == null || sortBy.isBlank() ? "fechaAplicacion" : sortBy;
        if (!SORT_FIELDS.contains(field)) {
            throw new BadRequestException("ORDENAMIENTO_INVALIDO", "El campo sortBy no está permitido");
        }
        Sort.Direction parsedDirection = direction == null || direction.isBlank()
                ? Sort.Direction.DESC : VacunaService.parseDirection(direction);
        List<Sort.Order> orders = new ArrayList<>();
        Sort.Order primary = new Sort.Order(parsedDirection, field);
        if (Set.of("mascotaNombre", "vacunaNombre", "veterinarioNombre").contains(field)) {
            primary = primary.ignoreCase();
        }
        orders.add(primary);
        if (!"id".equals(field)) {
            orders.add(new Sort.Order(parsedDirection, "id"));
        }
        return PageRequest.of(page, size, Sort.by(orders));
    }

    private ExternalServiceException availability(RetryableException exception, String service) {
        if (hasCause(exception, SocketTimeoutException.class)
                || hasCause(exception, HttpTimeoutException.class)
                || hasCause(exception, TimeoutException.class)) {
            return new ExternalServiceException(HttpStatus.GATEWAY_TIMEOUT, "SERVICIO_EXTERNO_TIMEOUT",
                    "Se agotó el tiempo de espera al consultar " + service);
        }
        return new ExternalServiceException(HttpStatus.SERVICE_UNAVAILABLE,
                "SERVICIO_EXTERNO_NO_DISPONIBLE", "El servicio externo " + service + " no está disponible");
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private ExternalServiceException dependencyFailure(String service) {
        return new ExternalServiceException(HttpStatus.BAD_GATEWAY, "FALLO_SERVICIO_EXTERNO",
                "No fue posible procesar la respuesta de " + service);
    }

    private ExternalServiceException feignFailure(FeignException exception, String service) {
        if (exception.status() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
            return new ExternalServiceException(HttpStatus.SERVICE_UNAVAILABLE,
                    "SERVICIO_EXTERNO_NO_DISPONIBLE",
                    "El servicio externo " + service + " no está disponible");
        }
        if (exception.status() == HttpStatus.GATEWAY_TIMEOUT.value()) {
            return new ExternalServiceException(HttpStatus.GATEWAY_TIMEOUT,
                    "SERVICIO_EXTERNO_TIMEOUT",
                    "Se agotó el tiempo de espera al consultar " + service);
        }
        return dependencyFailure(service);
    }

    private ExternalServiceException invalidResponse(String service) {
        return new ExternalServiceException(HttpStatus.BAD_GATEWAY, "RESPUESTA_SERVICIO_INVALIDA",
                "El servicio externo " + service + " devolvió una respuesta inválida");
    }

    private ExternalServiceException missingAuthenticationContext() {
        return new ExternalServiceException(HttpStatus.BAD_GATEWAY, "CONTEXTO_AUTENTICACION_NO_DISPONIBLE",
                "No fue posible propagar la autenticación al servicio externo");
    }

    private String fullName(VeterinarioDto veterinario) {
        return (veterinario.nombre().trim() + " " + veterinario.apellido().trim()).trim();
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private AplicacionVacunaResponse toResponse(AplicacionVacuna value) {
        return new AplicacionVacunaResponse(value.getId(), value.getVacunaId(), value.getMascotaId(),
                value.getVeterinarioId(), value.getFechaAplicacion(), value.getProximaDosis(),
                value.getLote(), value.getObservacion(), value.getMascotaNombre(),
                value.getVeterinarioNombre(), value.getVacunaNombre(), value.getEstado(),
                value.getMotivoAnulacion(), value.getFechaAnulacion());
    }
}
