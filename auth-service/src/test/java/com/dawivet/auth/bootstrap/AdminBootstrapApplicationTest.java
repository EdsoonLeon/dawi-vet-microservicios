package com.dawivet.auth.bootstrap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.dawivet.auth.repository.IRolRepository;
import com.dawivet.auth.repository.IUsuarioRepository;

@SpringBootTest(webEnvironment = WebEnvironment.NONE, properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "dawivet.bootstrap.admin.enabled=true",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
                + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.data.jpa.autoconfigure.JpaRepositoriesAutoConfiguration"
})
@Import(AdminBootstrapApplicationTest.RepositoryConfig.class)
class AdminBootstrapApplicationTest {

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Test
    void modoBootstrapIniciaSinServidorWeb() {
        verify(usuarioRepository).existsByRolNombre("ADMIN");
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class RepositoryConfig {

        @Bean
        IUsuarioRepository usuarioRepository() {
            IUsuarioRepository repository = mock(IUsuarioRepository.class);
            when(repository.existsByRolNombre("ADMIN")).thenReturn(true);
            return repository;
        }

        @Bean
        IRolRepository rolRepository() {
            return mock(IRolRepository.class);
        }
    }
}
