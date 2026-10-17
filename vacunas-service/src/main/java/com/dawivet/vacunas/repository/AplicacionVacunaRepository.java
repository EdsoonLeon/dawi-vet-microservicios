package com.dawivet.vacunas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.dawivet.vacunas.model.AplicacionVacuna;

public interface AplicacionVacunaRepository
        extends JpaRepository<AplicacionVacuna, Integer>, JpaSpecificationExecutor<AplicacionVacuna> {
}
