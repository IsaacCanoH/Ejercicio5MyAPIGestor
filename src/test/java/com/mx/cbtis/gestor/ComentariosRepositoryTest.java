package com.mx.cbtis.gestor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.mx.cbtis.gestor.modelo.Comentarios;
import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.repository.ComentariosRepository;
import com.mx.cbtis.gestor.repository.TareasRepository;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=none")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ComentariosRepositoryTest {

    @Autowired
    private ComentariosRepository comentariosRepository;

    @Autowired
    private TareasRepository tareasRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void insertarYSeleccionarComentario() {
        Tareas tarea = new Tareas();
        tarea.setNombre("Tarea para probar comentarios");
        Integer idTarea = tareasRepository.save(tarea).getId_tarea();

        long cantidadAntes = comentariosRepository.count();

        Comentarios comentario = new Comentarios();
        comentario.setId_usuario("Usuario de prueba");
        comentario.setId_tarea(idTarea);
        comentario.setTexto_comentario("Comentario de prueba");
        comentario.setFecha_hora("2026-10-06 12:00:00");

        Comentarios guardado = comentariosRepository.save(comentario);
        assertNotNull(guardado.getId_comentario());

        entityManager.flush();
        entityManager.clear();

        assertEquals(cantidadAntes + 1, comentariosRepository.count());

        Comentarios encontrado = comentariosRepository.findById(guardado.getId_comentario()).orElseThrow(
                () -> new AssertionError("No se encontró el comentario insertado"));
        assertEquals("Comentario de prueba", encontrado.getTexto_comentario());
        assertEquals(idTarea, encontrado.getId_tarea());

        assertTrue(comentariosRepository.findByTarea(idTarea).stream()
                .anyMatch(comentarioDeLaTarea -> comentarioDeLaTarea.getId_comentario()
                        .equals(guardado.getId_comentario())));
    }
}
