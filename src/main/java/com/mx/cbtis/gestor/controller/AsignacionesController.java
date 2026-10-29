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

import com.mx.cbtis.gestor.dto.AsignacionActualizacion;
import com.mx.cbtis.gestor.dto.AsignacionCreacion;
import com.mx.cbtis.gestor.dto.AsignacionId;
import com.mx.cbtis.gestor.dto.ValidarSolicitud;
import com.mx.cbtis.gestor.modelo.Asignaciones;
import com.mx.cbtis.gestor.repository.AsignacionesRepository;

@RestController
public class AsignacionesController {

    private final AsignacionesRepository asignacionestable;

    public AsignacionesController(AsignacionesRepository asignacionestable) {
        this.asignacionestable = asignacionestable;
    }

    @GetMapping("/buscarAsignacion")
    public Asignaciones getAsignacion(@RequestParam int id) {
        return asignacionestable.findById(id).orElseGet(Asignaciones::new);
    }

    @PostMapping("/addAsignacion")
    public int addAsignacion(@RequestBody AsignacionCreacion solicitud) {
        if (solicitud == null) {
            return 0;
        }
        ValidarSolicitud.id(solicitud.idUsuario());
        ValidarSolicitud.id(solicitud.idTarea());
        Asignaciones registro = new Asignaciones(null,
                solicitud.idUsuario(), solicitud.idTarea(), solicitud.fechaAsignacion());
        asignacionestable.save(registro);
        return 1;
    }

    @PostMapping("/updateAsignacion")
    public int updateAsignacion(@RequestBody AsignacionActualizacion solicitud) {
        if (solicitud == null) {
            return 0;
        }
        int id = ValidarSolicitud.id(solicitud.idAsignacion());
        Asignaciones registro = asignacionestable.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El registro no existe"));
        ValidarSolicitud.id(solicitud.idUsuario());
        registro.setId_usuario(solicitud.idUsuario());
        asignacionestable.save(registro);
        return 1;
    }

    @DeleteMapping("/deleteAsignacion")
    public int deleteAsignacion(@RequestBody AsignacionId solicitud) {
        if (solicitud == null) {
            return 0;
        }
        int id = ValidarSolicitud.id(solicitud.idAsignacion());
        Asignaciones registro = asignacionestable.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El registro no existe"));
        asignacionestable.delete(registro);
        return 1;
    }

    @PostMapping("/buscarAsignaciones")
    public List<Asignaciones> getAsignaciones() {
        List<Asignaciones> registros = new ArrayList<>();
        asignacionestable.findAll().forEach(registros::add);
        return registros;
    }
}
