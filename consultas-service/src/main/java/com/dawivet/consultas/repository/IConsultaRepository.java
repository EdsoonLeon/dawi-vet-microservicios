package com.dawivet.consultas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dawivet.consultas.model.Consulta;

@Repository
public interface IConsultaRepository extends JpaRepository<Consulta, Integer> {

    List<Consulta> findByMascotaId(Integer mascotaId);

    List<Consulta> findByVeterinarioId(Integer veterinarioId);
}