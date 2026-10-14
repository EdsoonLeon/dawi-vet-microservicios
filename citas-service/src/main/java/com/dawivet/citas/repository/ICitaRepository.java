package com.dawivet.citas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dawivet.citas.model.Cita;

@Repository
public interface ICitaRepository extends JpaRepository<Cita, Integer> {

    List<Cita> findByMascotaId(Integer mascotaId);

    List<Cita> findByVeterinarioId(Integer veterinarioId);
}