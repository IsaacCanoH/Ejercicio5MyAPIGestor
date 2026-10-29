package com.mx.cbtis.gestor.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class CredencialesDtoTest {
    private static final String CLAVE = "secreto-exclusivo-prueba";

    @Test
    void elLoginNoExponeCredencialesEnSuRepresentacionDeTexto() {
        String texto = new UsuarioLogin("privado@example.test", CLAVE).toString();
        assertFalse(texto.contains(CLAVE));
        assertFalse(texto.contains("privado@example.test"));
    }

    @Test
    void elRegistroNoExponeLaClaveEnSuRepresentacionDeTexto() {
        String texto = new UsuarioCreacion("Ana", "privado@example.test", CLAVE,
                RolRegistro.ESTUDIANTE, "#FFFFFF").toString();
        assertFalse(texto.contains(CLAVE));
        assertFalse(texto.contains("privado@example.test"));
    }

    @Test
    void laActualizacionNoExponeLaClaveEnSuRepresentacionDeTexto() {
        String texto = new UsuarioActualizacion(1, "Ana", "privado@example.test", CLAVE, "#FFFFFF").toString();
        assertFalse(texto.contains(CLAVE));
        assertFalse(texto.contains("privado@example.test"));
    }
}
