package com.dawivet.auth.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dawivet.auth.model.Rol;
import com.dawivet.auth.repository.IRolRepository;

@Service
public class RolService {

    @Autowired
    private IRolRepository repoRol;

    public List<Rol> listarRoles() {
        return repoRol.findAll();
    }
}