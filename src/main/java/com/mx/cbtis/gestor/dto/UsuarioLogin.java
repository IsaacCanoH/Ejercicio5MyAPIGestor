package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Datos de entrada permitidos; no es una entidad de base de datos. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioLogin(
        @JsonProperty("correo_electronico") String correoElectronico,
        String contrasena) {

    @Override
    public String toString() {
        return "UsuarioLogin[credenciales omitidas]";
    }
}
