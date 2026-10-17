package com.dawivet.vacunas.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.dawivet.vacunas.config.FeignConfig;
import com.dawivet.vacunas.dto.MascotaDto;

@FeignClient(name = "mascotas-service", configuration = FeignConfig.class)
public interface MascotaClient {

    @GetMapping("/api/mascotas/{id}")
    MascotaDto obtenerPorId(@PathVariable("id") Integer id);
}
