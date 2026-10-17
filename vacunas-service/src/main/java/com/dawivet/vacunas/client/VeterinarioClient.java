package com.dawivet.vacunas.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.dawivet.vacunas.config.FeignConfig;
import com.dawivet.vacunas.dto.VeterinarioDto;

@FeignClient(name = "veterinarios-service", configuration = FeignConfig.class)
public interface VeterinarioClient {

    @GetMapping("/api/veterinarios/{id}")
    VeterinarioDto obtenerPorId(@PathVariable("id") Integer id);
}
