package com.dawivet.clientes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dawivet.clientes.model.Cliente;

@Repository
public interface IClienteRepository extends JpaRepository<Cliente, Integer> {

    List<Cliente> findByActivoTrue();
}