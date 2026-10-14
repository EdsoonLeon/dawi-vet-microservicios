package com.dawivet.mascotas.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.dawivet.mascotas.dto.ClienteDto;

// "clientes-service" debe coincidir con spring.application.name de clientes-service
@FeignClient(name = "clientes-service")
public interface ClienteClient {

    @GetMapping("/api/clientes/{id}")
    ClienteDto obtenerPorId(@PathVariable("id") Integer id);
}