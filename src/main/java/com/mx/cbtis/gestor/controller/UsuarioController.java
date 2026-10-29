package com.mx.cbtis.gestor.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.mx.cbtis.gestor.dto.UsuarioActualizacion;
import com.mx.cbtis.gestor.dto.UsuarioCreacion;
import com.mx.cbtis.gestor.dto.UsuarioId;
import com.mx.cbtis.gestor.dto.UsuarioLogin;
import com.mx.cbtis.gestor.dto.ValidarSolicitud;
import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

@RestController
public class UsuarioController {

    private final UsuarioRepository usuariotable;

    public UsuarioController(UsuarioRepository usuariotable) {
        this.usuariotable = usuariotable;
    }

    @PostMapping("/getUsuarioLogin")
    public Usuario getUsuarioLogin(@RequestBody UsuarioLogin solicitud) {
        ValidarSolicitud.texto(solicitud.correoElectronico(), solicitud.contrasena());
        return usuariotable.findByCorreoPass(solicitud.correoElectronico(), solicitud.contrasena())
                .orElseGet(() -> {
                    Usuario rechazado = new Usuario();
                    rechazado.setNombre("000");
                    return rechazado;
                });
    }

    @GetMapping("/buscarUsuario")
    public Usuario getUsuario(@RequestParam int id) {
        return usuariotable.findById(id).orElseGet(Usuario::new);
    }

    @PostMapping("/addUsuario")
    public int addUsuario(@RequestBody UsuarioCreacion solicitud) {
        if (solicitud == null) {
            return 0;
        }
        ValidarSolicitud.texto(solicitud.nombre(), solicitud.correoElectronico(), solicitud.contrasena());
        Usuario registro = new Usuario(null,
                solicitud.nombre(), solicitud.correoElectronico(), solicitud.contrasena(), solicitud.rol() == null ? "Estudiante" : solicitud.rol().etiqueta(), solicitud.imagenUsuario());
        usuariotable.save(registro);
        return 1;
    }

    @PostMapping("/updateUsuario")
    public int updateUsuario(@RequestBody UsuarioActualizacion solicitud) {
        if (solicitud == null) {
            return 0;
        }
        int id = ValidarSolicitud.id(solicitud.idUsuario());
        Usuario registro = usuariotable.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El registro no existe"));
        ValidarSolicitud.texto(solicitud.nombre(), solicitud.correoElectronico(), solicitud.contrasena());
        registro.setNombre(solicitud.nombre());
        registro.setCorreo_electronico(solicitud.correoElectronico());
        registro.setContrasena(solicitud.contrasena());
        registro.setImagen_usuario(solicitud.imagenUsuario());
        usuariotable.save(registro);
        return 1;
    }

    @DeleteMapping("/deleteUsuario")
    public int deleteUsuario(@RequestBody UsuarioId solicitud) {
        if (solicitud == null) {
            return 0;
        }
        int id = ValidarSolicitud.id(solicitud.idUsuario());
        Usuario registro = usuariotable.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El registro no existe"));
        usuariotable.delete(registro);
        return 1;
    }

    @PostMapping("/buscarUsuarios")
    public List<Usuario> getUsuarios() {
        List<Usuario> registros = new ArrayList<>();
        usuariotable.findAll().forEach(registros::add);
        return registros;
    }
}
