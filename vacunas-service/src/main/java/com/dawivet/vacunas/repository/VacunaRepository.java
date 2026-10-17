package com.dawivet.vacunas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.dawivet.vacunas.model.Vacuna;

public interface VacunaRepository extends JpaRepository<Vacuna, Integer>, JpaSpecificationExecutor<Vacuna> {
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Integer id);
}
