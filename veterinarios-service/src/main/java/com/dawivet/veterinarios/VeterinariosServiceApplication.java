package com.dawivet.veterinarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class VeterinariosServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(VeterinariosServiceApplication.class, args);
	}

}
