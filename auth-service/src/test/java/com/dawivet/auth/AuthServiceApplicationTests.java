package com.dawivet.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.dawivet.auth.repository.IRolRepository;
import com.dawivet.auth.repository.IUsuarioRepository;

@SpringBootTest(properties = {
		"eureka.client.enabled=false",
		"spring.autoconfigure.exclude="
				+ "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
				+ "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
				+ "org.springframework.boot.data.jpa.autoconfigure.JpaRepositoriesAutoConfiguration"
})
class AuthServiceApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@MockitoBean
	private IUsuarioRepository usuarioRepository;

	@MockitoBean
	private IRolRepository rolRepository;

	@Test
	void contextLoads() {
		assertFalse(applicationContext.containsBean("adminBootstrap"));
	}

}
