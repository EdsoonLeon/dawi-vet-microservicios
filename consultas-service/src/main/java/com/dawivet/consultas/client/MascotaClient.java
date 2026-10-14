package com.dawivet.consultas.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.dawivet.consultas.dto.MascotaDto;

// Debe coincidir con spring.application.name de mascotas-service
@FeignClient(name = "mascotas-service")
public interface MascotaClient {

    @GetMapping("/api/mascotas/{id}")
    MascotaDto obtenerPorId(@PathVariable("id") Integer id);
}