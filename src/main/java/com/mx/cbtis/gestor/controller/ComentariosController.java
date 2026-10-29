package com.mx.cbtis.gestor.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.mx.cbtis.gestor.dto.ComentarioActualizacion;
import com.mx.cbtis.gestor.dto.ComentarioCreacion;
import com.mx.cbtis.gestor.dto.ComentarioId;
import com.mx.cbtis.gestor.dto.ComentariosPorTarea;
import com.mx.cbtis.gestor.dto.ValidarSolicitud;
import com.mx.cbtis.gestor.modelo.Comentarios;
import com.mx.cbtis.gestor.repository.ComentariosRepository;

@RestController
public class ComentariosController {

    private final ComentariosRepository comentariostable;

    public ComentariosController(ComentariosRepository comentariostable) {
        this.comentariostable = comentariostable;
    }

    @PostMapping("/getComentario")
    public List<Comentarios> getComentario(@RequestBody ComentariosPorTarea solicitud) {
        return comentariostable.findByTarea(ValidarSolicitud.id(solicitud.idTarea()));
    }

    @GetMapping("/buscarComentario")
    public Comentarios getComentario(@RequestParam int id) {
        return comentariostable.findById(id).orElseGet(Comentarios::new);
    }

    @PostMapping("/addComentario")
    public int addComentario(@RequestBody ComentarioCreacion solicitud) {
        if (solicitud == null) {
            return 0;
        }
        ValidarSolicitud.texto(solicitud.idUsuario(), solicitud.textoComentario());
        ValidarSolicitud.id(solicitud.idTarea());
        Comentarios registro = new Comentarios(null,
                solicitud.idUsuario(), solicitud.idTarea(), solicitud.textoComentario(), solicitud.fechaHora());
        comentariostable.save(registro);
        return 1;
    }

    @PostMapping("/updateComentario")
    public int updateComentario(@RequestBody ComentarioActualizacion solicitud) {
        if (solicitud == null) {
            return 0;
        }
        int id = ValidarSolicitud.id(solicitud.idComentario());
        Comentarios registro = comentariostable.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El registro no existe"));
        ValidarSolicitud.texto(solicitud.textoComentario());
        registro.setTexto_comentario(solicitud.textoComentario());
        comentariostable.save(registro);
        return 1;
    }

    @DeleteMapping("/deleteComentario")
    public int deleteComentario(@RequestBody ComentarioId solicitud) {
        if (solicitud == null) {
            return 0;
        }
        int id = ValidarSolicitud.id(solicitud.idComentario());
        Comentarios registro = comentariostable.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El registro no existe"));
        comentariostable.delete(registro);
        return 1;
    }

    @PostMapping("/buscarComentarios")
    public List<Comentarios> getComentarios() {
        List<Comentarios> registros = new ArrayList<>();
        comentariostable.findAll().forEach(registros::add);
        return registros;
    }
}
