package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Datos de entrada permitidos; no es una entidad de base de datos. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TareaActualizacion(
        @JsonProperty("id_tarea") Integer idTarea,
        String nombre,
        String descripcion,
        @JsonProperty("fecha_vencimiento") String fechaVencimiento,
        String lista,
        String estado) {
}
