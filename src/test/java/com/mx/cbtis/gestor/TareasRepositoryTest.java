package com.mx.cbtis.gestor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.mx.cbtis.gestor.modelo.Proyectos;
import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.repository.ProtectosRepository;
import com.mx.cbtis.gestor.repository.TareasRepository;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=none")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TareasRepositoryTest {

    @Autowired
    private TareasRepository tareasRepository;

    @Autowired
    private ProtectosRepository proyectosRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void insertarYSeleccionarTarea() {
        Proyectos proyecto = new Proyectos();
        proyecto.setNombre("Proyecto para probar tareas");
        Integer idProyecto = proyectosRepository.save(proyecto).getId_proyecto();

        long cantidadAntes = tareasRepository.count();

        Tareas tarea = new Tareas();
        tarea.setNombre("Tarea de prueba");
        tarea.setDescripcion("Verificar INSERT y SELECT");
        tarea.setFecha_vencimiento("2026-10-07");
        tarea.setLista("pendiente");
        tarea.setEstado("En proceso");
        tarea.setId_proyecto(idProyecto);

        Tareas guardada = tareasRepository.save(tarea);
        assertNotNull(guardada.getId_tarea());

        entityManager.flush();
        entityManager.clear();

        assertEquals(cantidadAntes + 1, tareasRepository.count());

        Tareas encontrada = tareasRepository.findById(guardada.getId_tarea()).orElseThrow(
                () -> new AssertionError("No se encontró la tarea insertada"));
        assertEquals("Tarea de prueba", encontrada.getNombre());
        assertEquals(idProyecto, encontrada.getId_proyecto());

        assertTrue(tareasRepository.findByIdProyecto(idProyecto).stream()
                .anyMatch(tareaDelProyecto -> tareaDelProyecto.getId_tarea().equals(guardada.getId_tarea())));
    }
}
