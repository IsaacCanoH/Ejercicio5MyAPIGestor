package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Datos de entrada permitidos; no es una entidad de base de datos. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ComentarioCreacion(
        @JsonProperty("id_usuario") String idUsuario,
        @JsonProperty("id_tarea") Integer idTarea,
        @JsonProperty("texto_comentario") String textoComentario,
        @JsonProperty("fecha_hora") String fechaHora) {
}
