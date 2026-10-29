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

import com.mx.cbtis.gestor.dto.TareaActualizacion;
import com.mx.cbtis.gestor.dto.TareaCreacion;
import com.mx.cbtis.gestor.dto.TareaId;
import com.mx.cbtis.gestor.dto.TareasPorProyecto;
import com.mx.cbtis.gestor.dto.ValidarSolicitud;
import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.repository.TareasRepository;

@RestController
public class TareasController {

    private final TareasRepository tareastable;

    public TareasController(TareasRepository tareastable) {
        this.tareastable = tareastable;
    }

    @PostMapping("/getTarea")
    public List<Tareas> getTarea(@RequestBody TareasPorProyecto solicitud) {
        return tareastable.findByIdProyecto(ValidarSolicitud.id(solicitud.idProyecto()));
    }

    @GetMapping("/buscarTarea")
    public Tareas getTarea(@RequestParam int id_tarea) {
        return tareastable.findById(id_tarea).orElseGet(Tareas::new);
    }

    @PostMapping("/addTareas")
    public int addTareas(@RequestBody TareaCreacion solicitud) {
        if (solicitud == null) {
            return 0;
        }
        ValidarSolicitud.texto(solicitud.nombre());
        ValidarSolicitud.id(solicitud.idProyecto());
        Tareas registro = new Tareas(null,
                solicitud.nombre(), solicitud.descripcion(), solicitud.fechaVencimiento(), solicitud.lista(), solicitud.estado(), solicitud.idProyecto());
        tareastable.save(registro);
        return 1;
    }

    @PostMapping("/updateTareas")
    public int updateTareas(@RequestBody TareaActualizacion solicitud) {
        if (solicitud == null) {
            return 0;
        }
        int id = ValidarSolicitud.id(solicitud.idTarea());
        Tareas registro = tareastable.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El registro no existe"));
        ValidarSolicitud.texto(solicitud.nombre());
        registro.setNombre(solicitud.nombre());
        registro.setDescripcion(solicitud.descripcion());
        registro.setFecha_vencimiento(solicitud.fechaVencimiento());
        registro.setLista(solicitud.lista());
        registro.setEstado(solicitud.estado());
        tareastable.save(registro);
        return 1;
    }

    @DeleteMapping("/deleteTareas")
    public int deleteTareas(@RequestBody TareaId solicitud) {
        if (solicitud == null) {
            return 0;
        }
        int id = ValidarSolicitud.id(solicitud.idTarea());
        Tareas registro = tareastable.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El registro no existe"));
        tareastable.delete(registro);
        return 1;
    }

    @PostMapping("/buscarTareas")
    public List<Tareas> getTareas() {
        List<Tareas> registros = new ArrayList<>();
        tareastable.findAll().forEach(registros::add);
        return registros;
    }
}
