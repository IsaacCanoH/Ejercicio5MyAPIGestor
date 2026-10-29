package com.mx.cbtis.gestor.dto;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class ValidarSolicitud {
    private ValidarSolicitud() {
    }

    public static int id(Integer valor) {
        if (valor == null || valor <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Se requiere un identificador positivo");
        }
        return valor;
    }

    public static void texto(String... valores) {
        for (String valor : valores) {
            if (valor == null || valor.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan datos obligatorios");
            }
        }
    }
}
