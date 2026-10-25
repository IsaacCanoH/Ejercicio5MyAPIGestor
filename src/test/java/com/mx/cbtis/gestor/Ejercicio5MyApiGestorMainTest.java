package com.mx.cbtis.gestor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.concurrent.atomic.AtomicReference;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.mx.cbtis.gestor.repository.AsignacionesRepository;
import com.mx.cbtis.gestor.repository.ComentariosRepository;
import com.mx.cbtis.gestor.repository.ProtectosRepository;
import com.mx.cbtis.gestor.repository.TareasRepository;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

class Ejercicio5MyApiGestorMainTest {

    private static final AtomicReference<ConfigurableApplicationContext> CONTEXTO = new AtomicReference<>();

    @Test
    void mainIniciaLaAplicacionYTransmiteLosArgumentos() {
        String[] argumentos = {
            "--spring.application.name=gestor-main-test",
            "--spring.main.web-application-type=none",
            "--spring.main.register-shutdown-hook=false",
            "--context.initializer.classes=" + ContextoSinBaseDeDatos.class.getName(),
            "--spring.autoconfigure.exclude=" + String.join(",",
                    DataSourceAutoConfiguration.class.getName(),
                    HibernateJpaAutoConfiguration.class.getName(),
                    JpaRepositoriesAutoConfiguration.class.getName())
        };

        try {
            // Ejecuta el main real; los argumentos aislan solo este contexto de prueba.
            Ejercicio5MyApiGestorApplication.main(argumentos);

            ConfigurableApplicationContext contexto = CONTEXTO.get();
            assertNotNull(contexto, "main debe crear el contexto de Spring");
            assertTrue(contexto.isActive());
            assertNotNull(contexto.getBean(Ejercicio5MyApiGestorApplication.class));
            assertEquals("gestor-main-test", contexto.getEnvironment().getProperty("spring.application.name"));
            assertFalse(contexto instanceof WebServerApplicationContext);
            assertEquals(0, contexto.getBeanNamesForType(DataSource.class).length);
        } finally {
            ConfigurableApplicationContext contexto = CONTEXTO.getAndSet(null);
            if (contexto != null) {
                contexto.close();
            }
        }
    }

    // Se carga exclusivamente mediante los argumentos de esta prueba, no por component scanning.
    public static class ContextoSinBaseDeDatos
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext contexto) {
            CONTEXTO.set(contexto);
            var beans = contexto.getBeanFactory();
            beans.registerSingleton("usuarioRepository", mock(UsuarioRepository.class));
            beans.registerSingleton("protectosRepository", mock(ProtectosRepository.class));
            beans.registerSingleton("tareasRepository", mock(TareasRepository.class));
            beans.registerSingleton("comentariosRepository", mock(ComentariosRepository.class));
            beans.registerSingleton("asignacionesRepository", mock(AsignacionesRepository.class));
        }
    }
}
