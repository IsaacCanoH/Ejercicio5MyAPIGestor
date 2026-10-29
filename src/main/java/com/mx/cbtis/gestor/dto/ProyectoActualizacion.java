package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Datos de entrada permitidos; no es una entidad de base de datos. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProyectoActualizacion(
        @JsonProperty("id_proyecto") Integer idProyecto,
        String nombre,
        String descripcion,
        @JsonProperty("fecha_inicio") String fechaInicio,
        @JsonProperty("fecha_finalizacion") String fechaFinalizacion,
        @JsonProperty("estado_proyecto") String estadoProyecto,
        String fondo) {
}
