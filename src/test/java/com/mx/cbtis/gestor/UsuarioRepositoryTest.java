package com.mx.cbtis.gestor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=none")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void insertarYSeleccionarUsuario() {
        long cantidadAntes = usuarioRepository.count();

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario de prueba");
        usuario.setCorreo_electronico("usuario.prueba@example.com");
        usuario.setContrasena("clave-de-prueba");
        usuario.setRol("Estudiante");
        usuario.setImagen_usuario("#FFFFFF");

        Usuario guardado = usuarioRepository.save(usuario);
        assertNotNull(guardado.getId_usuario());

        entityManager.flush();
        entityManager.clear();

        assertEquals(cantidadAntes + 1, usuarioRepository.count());

        Usuario encontrado = usuarioRepository.findById(guardado.getId_usuario()).orElseThrow(
                () -> new AssertionError("No se encontró el usuario insertado"));
        assertEquals("Usuario de prueba", encontrado.getNombre());

        assertTrue(usuarioRepository.findByCorreoPass("usuario.prueba@example.com", "clave-de-prueba")
                .map(usuarioPorCorreo -> usuarioPorCorreo.getId_usuario().equals(guardado.getId_usuario()))
                .orElse(false));
    }
}
