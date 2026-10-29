package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Datos de entrada permitidos; no es una entidad de base de datos. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioActualizacion(
        @JsonProperty("id_usuario") Integer idUsuario,
        String nombre,
        @JsonProperty("correo_electronico") String correoElectronico,
        String contrasena,
        @JsonProperty("imagen_usuario") String imagenUsuario) {

    @Override
    public String toString() {
        return "UsuarioActualizacion[credenciales omitidas]";
    }
}
