package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Datos de entrada permitidos; no es una entidad de base de datos. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AsignacionCreacion(
        @JsonProperty("id_usuario") Integer idUsuario,
        @JsonProperty("id_tarea") Integer idTarea,
        @JsonProperty("fecha_asignacion") String fechaAsignacion) {
}
