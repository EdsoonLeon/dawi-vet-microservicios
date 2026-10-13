package com.dawivet.auth.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dawivet.auth.model.Rol;
import com.dawivet.auth.service.RolService;

@RestController
@RequestMapping("/api/roles")
public class RolController {

    @Autowired
    private RolService service;

    @GetMapping
    public ResponseEntity<?> listarRoles() {
        List<Rol> roles = service.listarRoles();
        return ResponseEntity.ok(roles);
    }
}