package com.dawivet.veterinarios.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dawivet.veterinarios.model.Veterinario;

@Repository
public interface IVeterinarioRepository extends JpaRepository<Veterinario, Integer> {

    List<Veterinario> findByActivoTrue();
}