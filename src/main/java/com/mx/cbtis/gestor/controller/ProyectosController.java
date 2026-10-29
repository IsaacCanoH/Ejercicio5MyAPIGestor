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

import com.mx.cbtis.gestor.dto.ProyectoActualizacion;
import com.mx.cbtis.gestor.dto.ProyectoCreacion;
import com.mx.cbtis.gestor.dto.ProyectoId;
import com.mx.cbtis.gestor.dto.ValidarSolicitud;
import com.mx.cbtis.gestor.modelo.Proyectos;
import com.mx.cbtis.gestor.repository.ProtectosRepository;

@RestController
public class ProyectosController {

    private final ProtectosRepository proyectostable;

    public ProyectosController(ProtectosRepository proyectostable) {
        this.proyectostable = proyectostable;
    }

    @GetMapping("/buscarProyecto")
    public Proyectos getProyecto(@RequestParam int id_proyecto) {
        return proyectostable.findById(id_proyecto).orElseGet(Proyectos::new);
    }

    @PostMapping("/addProyecto")
    public int addProyecto(@RequestBody ProyectoCreacion solicitud) {
        if (solicitud == null) {
            return 0;
        }
        ValidarSolicitud.texto(solicitud.nombre());
        Proyectos registro = new Proyectos(null,
                solicitud.nombre(), solicitud.descripcion(), solicitud.fechaInicio(), solicitud.fechaFinalizacion(), solicitud.estadoProyecto(), solicitud.fondo());
        proyectostable.save(registro);
        return 1;
    }

    @PostMapping("/updateProyecto")
    public int updateProyecto(@RequestBody ProyectoActualizacion solicitud) {
        if (solicitud == null) {
            return 0;
        }
        int id = ValidarSolicitud.id(solicitud.idProyecto());
        Proyectos registro = proyectostable.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El registro no existe"));
        ValidarSolicitud.texto(solicitud.nombre());
        registro.setNombre(solicitud.nombre());
        registro.setDescripcion(solicitud.descripcion());
        registro.setFecha_inicio(solicitud.fechaInicio());
        registro.setFecha_finalizacion(solicitud.fechaFinalizacion());
        registro.setEstado_proyecto(solicitud.estadoProyecto());
        registro.setFondo(solicitud.fondo());
        proyectostable.save(registro);
        return 1;
    }

    @DeleteMapping("/deleteProyecto")
    public int deleteProyecto(@RequestBody ProyectoId solicitud) {
        if (solicitud == null) {
            return 0;
        }
        int id = ValidarSolicitud.id(solicitud.idProyecto());
        Proyectos registro = proyectostable.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El registro no existe"));
        proyectostable.delete(registro);
        return 1;
    }

    @PostMapping("/buscarProyectos")
    public List<Proyectos> getProyectos() {
        List<Proyectos> registros = new ArrayList<>();
        proyectostable.findAll().forEach(registros::add);
        return registros;
    }
}
