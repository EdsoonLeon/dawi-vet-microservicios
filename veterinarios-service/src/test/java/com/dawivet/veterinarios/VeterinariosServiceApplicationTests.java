package com.dawivet.veterinarios;

import com.dawivet.veterinarios.client.AuthClient;
import com.dawivet.veterinarios.repository.IVeterinarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
		"eureka.client.enabled=false",
		"spring.cloud.discovery.enabled=false",
		"spring.autoconfigure.exclude="
				+ "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
				+ "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
				+ "org.springframework.boot.data.jpa.autoconfigure.JpaRepositoriesAutoConfiguration"
})
class VeterinariosServiceApplicationTests {

	@MockitoBean
	private IVeterinarioRepository veterinarioRepository;

	@MockitoBean
	private AuthClient authClient;

	@Test
	void contextLoads() {
	}

}
