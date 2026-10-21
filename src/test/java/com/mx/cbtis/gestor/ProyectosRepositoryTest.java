package com.mx.cbtis.gestor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.mx.cbtis.gestor.modelo.Proyectos;
import com.mx.cbtis.gestor.repository.ProtectosRepository;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=none")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProyectosRepositoryTest {

    @Autowired
    private ProtectosRepository proyectosRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void insertarYSeleccionarProyecto() {
        long cantidadAntes = proyectosRepository.count();

        Proyectos proyecto = new Proyectos();
        proyecto.setNombre("Proyecto de prueba");
        proyecto.setDescripcion("Prueba de conexión con la tabla proyectos");
        proyecto.setFecha_inicio("2026-10-06");
        proyecto.setFecha_finalizacion("2026-10-07");
        proyecto.setEstado_proyecto("En proceso");
        proyecto.setFondo("#FFFFFF");

        Proyectos guardado = proyectosRepository.save(proyecto);
        assertNotNull(guardado.getId_proyecto());

        entityManager.flush();
        entityManager.clear();

        assertEquals(cantidadAntes + 1, proyectosRepository.count());

        Proyectos encontrado = proyectosRepository.findById(guardado.getId_proyecto()).orElseThrow(
                () -> new AssertionError("No se encontró el proyecto insertado"));
        assertEquals("Proyecto de prueba", encontrado.getNombre());
        assertEquals("En proceso", encontrado.getEstado_proyecto());
    }
}
