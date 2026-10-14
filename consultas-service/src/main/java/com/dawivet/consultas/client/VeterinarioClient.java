package com.dawivet.consultas.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.dawivet.consultas.dto.VeterinarioDto;

// Debe coincidir con spring.application.name de veterinarios-service
@FeignClient(name = "veterinarios-service")
public interface VeterinarioClient {

    @GetMapping("/api/veterinarios/{id}")
    VeterinarioDto obtenerPorId(@PathVariable("id") Integer id);
}