package com.dawivet.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dawivet.auth.model.Usuario;

@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Integer id);

    boolean existsByRolNombre(String nombre);

    // PARA LISTAR SOLO LOS USUARIOS ACTIVOS
    List<Usuario> findByActivoTrue();
}
