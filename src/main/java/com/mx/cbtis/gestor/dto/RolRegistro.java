package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonValue;

/** Categorias del formulario de registro; no conceden permisos administrativos. */
public enum RolRegistro {
    ESTUDIANTE("Estudiante"),
    DOCENTE("Docente"),
    OTRO("Otro");

    private final String etiqueta;

    RolRegistro(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @JsonValue
    public String etiqueta() {
        return etiqueta;
    }
}
