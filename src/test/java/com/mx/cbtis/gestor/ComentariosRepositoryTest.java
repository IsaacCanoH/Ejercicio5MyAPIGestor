package com.mx.cbtis.gestor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

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
    void filtraLosComentariosDeCadaTareaYDevuelveVacioCuandoNoTieneComentarios() {
        Tareas tareaA = new Tareas();
        tareaA.setNombre("Tarea A para probar comentarios");
        Integer idTareaA = tareasRepository.save(tareaA).getId_tarea();
        Tareas tareaB = new Tareas();
        tareaB.setNombre("Tarea B para probar comentarios");
        Integer idTareaB = tareasRepository.save(tareaB).getId_tarea();
        Tareas tareaVacia = new Tareas();
        tareaVacia.setNombre("Tarea sin comentarios");
        Integer idTareaVacia = tareasRepository.save(tareaVacia).getId_tarea();

        Comentarios comentarioA = new Comentarios();
        comentarioA.setTexto_comentario("Comentario exclusivo de la tarea A");
        comentarioA.setId_tarea(idTareaA);
        Integer idComentarioA = comentariosRepository.save(comentarioA).getId_comentario();
        Comentarios otroComentarioA = new Comentarios();
        otroComentarioA.setTexto_comentario("Segundo comentario de la tarea A");
        otroComentarioA.setId_tarea(idTareaA);
        Integer idOtroComentarioA = comentariosRepository.save(otroComentarioA).getId_comentario();
        Comentarios comentarioB = new Comentarios();
        comentarioB.setTexto_comentario("Comentario exclusivo de la tarea B");
        comentarioB.setId_tarea(idTareaB);
        Integer idComentarioB = comentariosRepository.save(comentarioB).getId_comentario();
        entityManager.flush();
        entityManager.clear();

        List<Integer> comentariosA = comentariosRepository.findByTarea(idTareaA).stream()
                .map(Comentarios::getId_comentario).toList();
        assertEquals(2, comentariosA.size(), "Deben devolverse todos los comentarios de A y ninguno de B");
        assertTrue(comentariosA.containsAll(List.of(idComentarioA, idOtroComentarioA)));
        assertEquals(List.of(idComentarioB), comentariosRepository.findByTarea(idTareaB).stream()
                .map(Comentarios::getId_comentario).toList(), "La tarea B no debe incluir comentarios de A");
        assertTrue(comentariosRepository.findByTarea(idTareaVacia).isEmpty(),
                "Una tarea sin comentarios debe devolver una lista vacia");
    }

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
