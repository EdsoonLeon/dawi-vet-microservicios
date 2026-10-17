package com.dawivet.vacunas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.dawivet.vacunas.exception.MissingAuthenticationContextException;

import feign.RequestInterceptor;
import feign.Retryer;

@Configuration
public class FeignConfig {

    @Bean
    RequestInterceptor authInterceptor() {
        return template -> {
            if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
                throw new MissingAuthenticationContextException();
            }
            String authorization = attrs.getRequest().getHeader("Authorization");
            if (authorization == null || authorization.isBlank()) {
                throw new MissingAuthenticationContextException();
            }
            template.header("Authorization", authorization);
        };
    }

    @Bean
    Retryer retryer() {
        return Retryer.NEVER_RETRY;
    }
}
