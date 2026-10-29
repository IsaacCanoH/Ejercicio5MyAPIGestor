package com.mx.cbtis.gestor.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.cbtis.gestor.modelo.Comentarios;
import com.mx.cbtis.gestor.modelo.Proyectos;
import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.AsignacionesRepository;
import com.mx.cbtis.gestor.repository.ComentariosRepository;
import com.mx.cbtis.gestor.repository.ProtectosRepository;
import com.mx.cbtis.gestor.repository.TareasRepository;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

/** Comprueba JSON, DTO, controladores y persistencia; cada caso se revierte al terminar. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SolicitudesDtoIntegrationTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private ProtectosRepository proyectos;
    @Autowired
    private TareasRepository tareas;
    @Autowired
    private ComentariosRepository comentarios;
    @Autowired
    private AsignacionesRepository asignaciones;

    @Test
    void usuarioNoSobrescribeOtroIdNiPermiteCambiarElRol() throws Exception {
        Usuario existente = usuarios.save(new Usuario(null, "Original", correo(), "clave-original", "Docente", "#FFFFFF"));
        int idExistente = existente.getId_usuario();
        String correoNuevo = correo();
        guardar("/addUsuario", Map.of("id_usuario", idExistente, "nombre", "Nuevo", "correo_electronico", correoNuevo,
                "contrasena", "clave-nueva", "rol", "Estudiante", "imagen_usuario", "#123456"));
        recargar();
        Usuario nuevo = usuarios.findByCorreoPass(correoNuevo, "clave-nueva").orElseThrow();
        int idNuevo = nuevo.getId_usuario();
        assertNotEquals(idExistente, idNuevo);
        assertEquals("Original", usuarios.findById(idExistente).orElseThrow().getNombre());

        mvc.perform(post("/getUsuarioLogin").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("correo_electronico", correoNuevo,
                        "contrasena", "clave-nueva", "rol", "Administrador"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id_usuario").value(idNuevo));

        guardar("/updateUsuario", Map.of("id_usuario", idNuevo, "nombre", "Actualizado", "correo_electronico", correoNuevo,
                "contrasena", "clave-nueva", "rol", "Administrador", "imagen_usuario", "#654321"));
        recargar();
        Usuario actualizado = usuarios.findById(idNuevo).orElseThrow();
        assertEquals("Actualizado", actualizado.getNombre());
        assertEquals("Estudiante", actualizado.getRol());
        eliminar("/deleteUsuario", "id_usuario", idNuevo);
        assertFalse(usuarios.existsById(idNuevo));
    }

    @Test
    void proyectoSeCreaEditaYEliminaSinUsarElIdDelAlta() throws Exception {
        Proyectos existente = proyectoBase();
        String nombreNuevo = "DTO-" + UUID.randomUUID();
        guardar("/addProyecto", Map.of("id_proyecto", existente.getId_proyecto(), "nombre", nombreNuevo,
                "descripcion", "Inicial", "fecha_inicio", "2026-10-28", "fecha_finalizacion", "2026-10-30",
                "estado_proyecto", "En proceso", "fondo", "#FFFFFF"));
        int idNuevo = buscarId("/buscarProyectos", "nombre", nombreNuevo, "id_proyecto");
        assertNotEquals(existente.getId_proyecto(), idNuevo);
        guardar("/updateProyecto", Map.of("id_proyecto", idNuevo, "nombre", nombreNuevo,
                "descripcion", "Editado", "fecha_inicio", "2026-10-28", "fecha_finalizacion", "2026-10-31",
                "estado_proyecto", "Terminado", "fondo", "#123456"));
        recargar();
        assertEquals("Editado", proyectos.findById(idNuevo).orElseThrow().getDescripcion());
        assertEquals("Base DTO", proyectos.findById(existente.getId_proyecto()).orElseThrow().getDescripcion());
        eliminar("/deleteProyecto", "id_proyecto", idNuevo);
        assertFalse(proyectos.existsById(idNuevo));
    }

    @Test
    void tareaConservaSuProyectoAunqueElJsonIntenteCambiarlo() throws Exception {
        Proyectos proyecto = proyectoBase();
        String nombre = "DTO-" + UUID.randomUUID();
        guardar("/addTareas", Map.of("nombre", nombre, "id_proyecto", proyecto.getId_proyecto(),
                "descripcion", "Inicial", "lista", "pendientes", "estado", "primary"));
        int id = buscarId("/buscarTareas", "nombre", nombre, "id_tarea");
        guardar("/updateTareas", Map.of("id_tarea", id, "nombre", nombre, "descripcion", "Editada",
                "lista", "encurso", "estado", "success", "id_proyecto", 999999));
        recargar();
        Tareas actualizada = tareas.findById(id).orElseThrow();
        assertEquals("Editada", actualizada.getDescripcion());
        assertEquals(proyecto.getId_proyecto(), actualizada.getId_proyecto());
        mvc.perform(post("/getTarea").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("id_proyecto", proyecto.getId_proyecto()))))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id_tarea").value(id));
        eliminar("/deleteTareas", "id_tarea", id);
        assertFalse(tareas.existsById(id));
    }

    @Test
    void comentarioSoloPermiteEditarElTexto() throws Exception {
        Tareas tarea = tareaBase();
        guardar("/addComentario", Map.of("id_tarea", tarea.getId_tarea(), "id_usuario", "Autor original",
                "texto_comentario", "Inicial", "fecha_hora", "2026-10-28 12:00:00"));
        int id = buscarId("/buscarComentarios", "id_tarea", tarea.getId_tarea().toString(), "id_comentario");
        guardar("/updateComentario", Map.of("id_comentario", id, "texto_comentario", "Editado",
                "id_usuario", "Otro autor", "id_tarea", 999999, "fecha_hora", "2099-01-01 00:00:00"));
        recargar();
        Comentarios actualizado = comentarios.findById(id).orElseThrow();
        assertEquals("Editado", actualizado.getTexto_comentario());
        assertEquals("Autor original", actualizado.getId_usuario());
        assertEquals(tarea.getId_tarea(), actualizado.getId_tarea());
        assertEquals("2026-10-28 12:00:00", actualizado.getFecha_hora());
        mvc.perform(post("/getComentario").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("id_tarea", tarea.getId_tarea()))))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id_comentario").value(id));
        eliminar("/deleteComentario", "id_comentario", id);
        assertFalse(comentarios.existsById(id));
    }

    @Test
    void asignacionConservaLaTareaYFechaAlCambiarElUsuario() throws Exception {
        Tareas tarea = tareaBase();
        Usuario primero = usuarios.save(new Usuario(null, "Primero", correo(), "clave", "Estudiante", "#FFFFFF"));
        Usuario segundo = usuarios.save(new Usuario(null, "Segundo", correo(), "clave", "Estudiante", "#FFFFFF"));
        guardar("/addAsignacion", Map.of("id_usuario", primero.getId_usuario(), "id_tarea", tarea.getId_tarea(),
                "fecha_asignacion", "2026-10-28"));
        int id = buscarId("/buscarAsignaciones", "id_tarea", tarea.getId_tarea().toString(), "id_asignacion");
        guardar("/updateAsignacion", Map.of("id_asignacion", id, "id_usuario", segundo.getId_usuario(),
                "id_tarea", 999999, "fecha_asignacion", "2099-01-01"));
        recargar();
        var actualizada = asignaciones.findById(id).orElseThrow();
        assertEquals(segundo.getId_usuario(), actualizada.getId_usuario());
        assertEquals(tarea.getId_tarea(), actualizada.getId_tarea());
        assertEquals("2026-10-28", actualizada.getFecha_asignacion());
        eliminar("/deleteAsignacion", "id_asignacion", id);
        assertFalse(asignaciones.existsById(id));
    }

    private void guardar(String ruta, Map<String, Object> datos) throws Exception {
        mvc.perform(post(ruta).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(datos)))
                .andExpect(status().isOk()).andExpect(content().string("1"));
        recargar();
    }

    private void eliminar(String ruta, String campo, int id) throws Exception {
        mvc.perform(delete(ruta).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(campo, id))))
                .andExpect(status().isOk()).andExpect(content().string("1"));
        recargar();
    }

    private int buscarId(String ruta, String campo, String valor, String identificador) throws Exception {
        String respuesta = mvc.perform(post(ruta)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (JsonNode registro : mapper.readTree(respuesta)) {
            if (valor.equals(registro.path(campo).asText())) {
                return registro.path(identificador).asInt();
            }
        }
        throw new AssertionError("El registro creado no aparece en " + ruta);
    }

    private Proyectos proyectoBase() {
        return proyectos.save(new Proyectos(null, "DTO-" + UUID.randomUUID(), "Base DTO",
                "2026-10-28", "2026-10-30", "En proceso", "#FFFFFF"));
    }

    private Tareas tareaBase() {
        return tareas.save(new Tareas(null, "DTO-" + UUID.randomUUID(), "Base DTO", "2026-10-30",
                "pendientes", "primary", proyectoBase().getId_proyecto()));
    }

    private String correo() {
        return "dto-" + UUID.randomUUID() + "@example.test";
    }

    private void recargar() {
        entityManager.flush();
        entityManager.clear();
    }
}
