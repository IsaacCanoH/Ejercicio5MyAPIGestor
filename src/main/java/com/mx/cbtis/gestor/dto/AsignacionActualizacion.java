package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Datos de entrada permitidos; no es una entidad de base de datos. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AsignacionActualizacion(
        @JsonProperty("id_asignacion") Integer idAsignacion,
        @JsonProperty("id_usuario") Integer idUsuario) {
}
