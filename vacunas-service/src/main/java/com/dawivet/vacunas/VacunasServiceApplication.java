package com.dawivet.vacunas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class VacunasServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(VacunasServiceApplication.class, args);
	}

}
