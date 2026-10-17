package com.dawivet.vacunas.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dawivet.vacunas.dto.PageResponse;
import com.dawivet.vacunas.dto.VacunaRequest;
import com.dawivet.vacunas.dto.VacunaResponse;
import com.dawivet.vacunas.exception.BadRequestException;
import com.dawivet.vacunas.exception.ConflictException;
import com.dawivet.vacunas.exception.ResourceNotFoundException;
import com.dawivet.vacunas.model.Vacuna;
import com.dawivet.vacunas.repository.VacunaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VacunaService {

    private static final Set<String> SORT_FIELDS = Set.of("id", "nombre", "enfermedadPreviene", "activo");

    private final VacunaRepository repository;

    @Transactional
    public VacunaResponse registrar(VacunaRequest request) {
        String nombre = normalizeRequired(request.nombre());
        if (repository.existsByNombreIgnoreCase(nombre)) {
            throw duplicated();
        }
        Vacuna vacuna = new Vacuna();
        apply(vacuna, request, nombre);
        vacuna.setActivo(true);
        return toResponse(save(vacuna));
    }

    @Transactional(readOnly = true)
    public PageResponse<VacunaResponse> buscar(
            int page, int size, String nombre, Boolean activo, String sortBy, String direction) {
        Pageable pageable = pageable(page, size, sortBy, direction);
        Specification<Vacuna> specification = (root, query, cb) -> cb.conjunction();

        if (nombre != null) {
            String filtro = nombre.trim();
            if (filtro.isEmpty()) {
                throw new BadRequestException("FILTRO_INVALIDO", "El filtro nombre no puede contener solo espacios");
            }
            specification = specification.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("nombre")), "%" + filtro.toLowerCase(Locale.ROOT) + "%"));
        }
        if (activo != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("activo"), activo));
        }

        Page<Vacuna> result = repository.findAll(specification, pageable);
        return PageResponse.from(result, result.getContent().stream().map(this::toResponse).toList());
    }

    @Transactional(readOnly = true)
    public VacunaResponse obtener(Integer id) {
        return toResponse(find(id));
    }

    @Transactional
    public VacunaResponse actualizar(Integer id, VacunaRequest request) {
        Vacuna vacuna = find(id);
        String nombre = normalizeRequired(request.nombre());
        if (repository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw duplicated();
        }
        apply(vacuna, request, nombre);
        return toResponse(save(vacuna));
    }

    @Transactional
    public VacunaResponse desactivar(Integer id) {
        Vacuna vacuna = find(id);
        if (!Boolean.TRUE.equals(vacuna.getActivo())) {
            throw new ConflictException("VACUNA_YA_INACTIVA", "La vacuna ya se encuentra inactiva");
        }
        vacuna.setActivo(false);
        return toResponse(repository.save(vacuna));
    }

    @Transactional
    public VacunaResponse activar(Integer id) {
        Vacuna vacuna = find(id);
        if (Boolean.TRUE.equals(vacuna.getActivo())) {
            throw new ConflictException("VACUNA_YA_ACTIVA", "La vacuna ya se encuentra activa");
        }
        vacuna.setActivo(true);
        return toResponse(repository.save(vacuna));
    }

    Vacuna find(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "VACUNA_NO_ENCONTRADA", "No se encontró la vacuna solicitada"));
    }

    private Pageable pageable(int page, int size, String sortBy, String direction) {
        validatePage(page, size);
        String field = sortBy == null || sortBy.isBlank() ? "nombre" : sortBy;
        if (!SORT_FIELDS.contains(field)) {
            throw new BadRequestException("ORDENAMIENTO_INVALIDO", "El campo sortBy no está permitido");
        }
        Sort.Direction sortDirection = parseDirection(direction);
        List<Sort.Order> orders = new ArrayList<>();
        Sort.Order primary = new Sort.Order(sortDirection, field);
        if ("nombre".equals(field) || "enfermedadPreviene".equals(field)) {
            primary = primary.ignoreCase();
        }
        orders.add(primary);
        if (!"id".equals(field)) {
            orders.add(new Sort.Order(sortDirection, "id"));
        }
        return PageRequest.of(page, size, Sort.by(orders));
    }

    static void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BadRequestException(
                    "PAGINACION_INVALIDA", "page debe ser mayor o igual a 0 y size debe estar entre 1 y 100");
        }
    }

    static Sort.Direction parseDirection(String direction) {
        String value = direction == null || direction.isBlank() ? "asc" : direction;
        if (!"asc".equalsIgnoreCase(value) && !"desc".equalsIgnoreCase(value)) {
            throw new BadRequestException("ORDENAMIENTO_INVALIDO", "direction debe ser asc o desc");
        }
        return Sort.Direction.fromString(value);
    }

    private void apply(Vacuna target, VacunaRequest request, String nombre) {
        target.setNombre(nombre);
        target.setDescripcion(normalizeOptional(request.descripcion()));
        target.setEnfermedadPreviene(normalizeRequired(request.enfermedadPreviene()));
    }

    private Vacuna save(Vacuna vacuna) {
        try {
            return repository.save(vacuna);
        } catch (DataIntegrityViolationException ex) {
            throw duplicated();
        }
    }

    private ConflictException duplicated() {
        return new ConflictException("VACUNA_DUPLICADA", "Ya existe una vacuna con el mismo nombre");
    }

    private String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private VacunaResponse toResponse(Vacuna vacuna) {
        return new VacunaResponse(vacuna.getId(), vacuna.getNombre(), vacuna.getDescripcion(),
                vacuna.getEnfermedadPreviene(), vacuna.getActivo());
    }
}
