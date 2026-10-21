package com.mx.cbtis.gestor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.mx.cbtis.gestor.modelo.Asignaciones;
import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.AsignacionesRepository;
import com.mx.cbtis.gestor.repository.TareasRepository;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=none")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AsignacionesRepositoryTest {

    @Autowired
    private AsignacionesRepository asignacionesRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TareasRepository tareasRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void insertarYSeleccionarAsignacion() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario para probar asignaciones");
        Integer idUsuario = usuarioRepository.save(usuario).getId_usuario();

        Tareas tarea = new Tareas();
        tarea.setNombre("Tarea para probar asignaciones");
        Integer idTarea = tareasRepository.save(tarea).getId_tarea();

        long cantidadAntes = asignacionesRepository.count();

        Asignaciones asignacion = new Asignaciones();
        asignacion.setId_usuario(idUsuario);
        asignacion.setId_tarea(idTarea);
        asignacion.setFecha_asignacion("2026-10-06");

        Asignaciones guardada = asignacionesRepository.save(asignacion);
        assertNotNull(guardada.getId_asignacion());

        entityManager.flush();
        entityManager.clear();

        assertEquals(cantidadAntes + 1, asignacionesRepository.count());

        Asignaciones encontrada = asignacionesRepository.findById(guardada.getId_asignacion()).orElseThrow(
                () -> new AssertionError("No se encontró la asignación insertada"));
        assertEquals(idUsuario, encontrada.getId_usuario());
        assertEquals(idTarea, encontrada.getId_tarea());
    }
}
