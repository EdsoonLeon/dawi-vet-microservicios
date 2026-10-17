package com.dawivet.veterinarios.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.dawivet.veterinarios.config.AuthFeignConfig;
import com.dawivet.veterinarios.dto.RolDto;
import com.dawivet.veterinarios.dto.UsuarioDto;
import com.dawivet.veterinarios.dto.UsuarioRegistroDto;

// "auth-service" debe coincidir con spring.application.name de auth-service
@FeignClient(name = "auth-service", configuration = AuthFeignConfig.class)
public interface AuthClient {

    @GetMapping("/api/roles")
    List<RolDto> listarRoles();

    @PostMapping("/api/auth/registrar")
    UsuarioDto registrar(@RequestBody UsuarioRegistroDto usuario);
}
