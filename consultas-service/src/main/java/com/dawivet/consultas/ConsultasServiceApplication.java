package com.dawivet.consultas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ConsultasServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConsultasServiceApplication.class, args);
	}

}
