package com.dawivet.mascotas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dawivet.mascotas.model.Mascota;

@Repository
public interface IMascotaRepository extends JpaRepository<Mascota, Integer> {

    List<Mascota> findByActivoTrue();

    List<Mascota> findByClienteId(Integer clienteId);
}