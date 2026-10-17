package com.dawivet.vacunas.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST,
                                "/api/vacunas/aplicaciones/registrar")
                            .hasAnyRole("ADMIN", "VETERINARIO")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/vacunas/aplicaciones/*")
                            .hasAnyRole("ADMIN", "VETERINARIO")
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/vacunas/aplicaciones/*/anular")
                            .hasAnyRole("ADMIN", "VETERINARIO")
                        .requestMatchers(HttpMethod.POST,
                                "/api/vacunas/registrar")
                            .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/vacunas/*")
                            .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/vacunas/*")
                            .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/vacunas/*/activar")
                            .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/vacunas/**")
                            .hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
