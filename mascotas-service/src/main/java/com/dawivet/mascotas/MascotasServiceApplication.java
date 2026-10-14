package com.dawivet.mascotas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MascotasServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MascotasServiceApplication.class, args);
	}

}
