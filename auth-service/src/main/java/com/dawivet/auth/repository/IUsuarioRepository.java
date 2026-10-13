package com.dawivet.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dawivet.auth.model.Usuario;

@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, Integer> {

    // BUSCADOR POR CORREO
    Optional<Usuario> findByCorreo(String correo);

    // PARA LISTAR SOLO LOS USUARIOS ACTIVOS
    List<Usuario> findByActivoTrue();
}