package com.mx.cbtis.gestor.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    private static final String JSON_REGISTRO = """
            {
                "id_usuario": 17,
                "nombre": "Ana",
                "correo_electronico": "ana@example.com",
                "contrasena": "clave-prueba",
                "rol": "Estudiante",
                "imagen_usuario": "#FFFFFF"
            }
            """;

    @Mock
    private UsuarioRepository repository;

    @InjectMocks
    private UsuarioController controller;

    @Captor
    private ArgumentCaptor<Usuario> registroCaptor;

    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;
    private Usuario registro;

    @BeforeEach
    void preparar() {
        registro = new Usuario(17, "Ana", "ana@example.com", "clave-prueba", "Estudiante", "#FFFFFF");
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void buscaUnRegistroExistente() throws Exception {
        when(repository.findById(17)).thenReturn(Optional.of(registro));

        mvc.perform(get("/buscarUsuario").param("id", "17"))
                .andExpect(status().isOk())
                .andExpect(content().json(JSON_REGISTRO, true));

        verify(repository).findById(17);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void devuelveObjetoVacioCuandoElRegistroNoExiste() throws Exception {
        when(repository.findById(999)).thenReturn(Optional.empty());

        // La API actual responde 200 con los campos a null cuando no encuentra el ID.
        mvc.perform(get("/buscarUsuario").param("id", "999"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(new Usuario()), true));

        verify(repository).findById(999);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void rechazaBusquedaSinIdentificador() throws Exception {
        mvc.perform(get("/buscarUsuario"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }

    @Test
    void rechazaIdentificadorQueNoEsUnNumero() throws Exception {
        mvc.perform(get("/buscarUsuario").param("id", "abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }

    @Test
    void creaRegistroSinIdentificadorYConservaSusDatos() throws Exception {
        var esperado = mapper.readTree(JSON_REGISTRO);
        ((ObjectNode) esperado).remove("id_usuario");

        mvc.perform(post("/addUsuario").contentType(MediaType.APPLICATION_JSON)
                .content(esperado.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(repository).save(registroCaptor.capture());
        var recibido = mapper.valueToTree(registroCaptor.getValue());
        ((ObjectNode) recibido).remove("id_usuario");
        assertEquals(esperado, recibido);
        assertNull(registroCaptor.getValue().getId_usuario());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void actualizaElRegistroConElIdentificadorYDatosRecibidos() throws Exception {
        when(repository.findById(17)).thenReturn(Optional.of(registro));

        mvc.perform(post("/updateUsuario").contentType(MediaType.APPLICATION_JSON)
                .content(JSON_REGISTRO))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(repository).findById(17);
        verify(repository).save(registroCaptor.capture());
        assertEquals(mapper.readTree(JSON_REGISTRO), mapper.valueToTree(registroCaptor.getValue()));
        verifyNoMoreInteractions(repository);
    }

    @Test
    void eliminaElRegistroIndicado() throws Exception {
        when(repository.findById(17)).thenReturn(Optional.of(registro));

        mvc.perform(delete("/deleteUsuario").contentType(MediaType.APPLICATION_JSON)
                .content(JSON_REGISTRO))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(repository).findById(17);
        verify(repository).delete(registroCaptor.capture());
        assertEquals(mapper.readTree(JSON_REGISTRO), mapper.valueToTree(registroCaptor.getValue()));
        verifyNoMoreInteractions(repository);
    }

    @Test
    void listaLosRegistrosConTodosSusCampos() throws Exception {
        when(repository.findAll()).thenReturn(List.of(registro));

        mvc.perform(post("/buscarUsuarios"))
                .andExpect(status().isOk())
                .andExpect(content().json("[" + JSON_REGISTRO + "]", true));

        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void devuelveListaVaciaCuandoNoHayRegistros() throws Exception {
        when(repository.findAll()).thenReturn(List.of());

        mvc.perform(post("/buscarUsuarios"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]", true));

        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void crearConNullDevuelveCeroSinUsarElRepositorio() {
        assertEquals(0, controller.addUsuario(null));
        verifyNoInteractions(repository);
    }

    @Test
    void actualizarConNullDevuelveCeroSinUsarElRepositorio() {
        assertEquals(0, controller.updateUsuario(null));
        verifyNoInteractions(repository);
    }

    @Test
    void eliminarConNullDevuelveCeroSinUsarElRepositorio() {
        assertEquals(0, controller.deleteUsuario(null));
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @CsvSource({
        "POST, /addUsuario",
        "POST, /updateUsuario",
        "DELETE, /deleteUsuario"
    })
    void rechazaCuerposAusentesNulosOMalformados(String metodo, String ruta) throws Exception {
        for (String cuerpo : List.of("", "null", "{")) {
            mvc.perform(request(HttpMethod.valueOf(metodo), ruta)
                    .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                    .andExpect(status().isBadRequest());
        }

        verifyNoInteractions(repository);
    }

    @Test
    void loginConCredencialesCorrectasDevuelveElUsuario() throws Exception {
        when(repository.findByCorreoPass("ana@example.com", "clave-prueba"))
                .thenReturn(Optional.of(registro));

        mvc.perform(post("/getUsuarioLogin").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo_electronico\":\"ana@example.com\",\"contrasena\":\"clave-prueba\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json(JSON_REGISTRO, true));

        verify(repository).findByCorreoPass("ana@example.com", "clave-prueba");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void loginConCredencialesIncorrectasDevuelveIndicadorDeRechazo() throws Exception {
        when(repository.findByCorreoPass("ana@example.com", "incorrecta"))
                .thenReturn(Optional.empty());

        mvc.perform(post("/getUsuarioLogin").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo_electronico\":\"ana@example.com\",\"contrasena\":\"incorrecta\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("000"))
                .andExpect(jsonPath("$.id_usuario").doesNotExist())
                .andExpect(jsonPath("$.contrasena").doesNotExist());

        verify(repository).findByCorreoPass("ana@example.com", "incorrecta");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void rechazaLoginSinCuerpo() throws Exception {
        mvc.perform(post("/getUsuarioLogin").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }


    @Test
    void ignoraElIdentificadorEnElAltaParaNoSobrescribirRegistros() throws Exception {
        when(repository.save(any(Usuario.class))).thenAnswer(invocacion -> {
            Usuario generado = invocacion.getArgument(0);
            assertNull(generado.getId_usuario(), "El ID enviado no debe llegar al repositorio");
            generado.setId_usuario(42);
            return generado;
        });

        mvc.perform(post("/addUsuario").contentType(MediaType.APPLICATION_JSON)
                .content(JSON_REGISTRO))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(repository).save(registroCaptor.capture());
        assertEquals(42, registroCaptor.getValue().getId_usuario());
        verifyNoMoreInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/updateUsuario", "/deleteUsuario"})
    void rechazaCambiosSinIdValido(String ruta) throws Exception {
        for (String cuerpo : List.of("{}", "{\"id_usuario\":null}",
                "{\"id_usuario\":0}", "{\"id_usuario\":-1}")) {
            mvc.perform(request(ruta.startsWith("/delete") ? HttpMethod.DELETE : HttpMethod.POST, ruta)
                    .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/updateUsuario", "/deleteUsuario"})
    void noCreaNiEliminaUnRegistroQueNoExiste(String ruta) throws Exception {
        when(repository.findById(17)).thenReturn(Optional.empty());

        mvc.perform(request(ruta.startsWith("/delete") ? HttpMethod.DELETE : HttpMethod.POST, ruta)
                .contentType(MediaType.APPLICATION_JSON).content(JSON_REGISTRO))
                .andExpect(status().isNotFound());

        verify(repository).findById(17);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void eliminaConSoloElIdentificadorYUsaLaEntidadDelServidor() throws Exception {
        when(repository.findById(17)).thenReturn(Optional.of(registro));

        mvc.perform(delete("/deleteUsuario").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_usuario\":17}"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(repository).findById(17);
        verify(repository).delete(registro);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void laActualizacionConservaLosCamposProtegidos() throws Exception {
        when(repository.findById(17)).thenReturn(Optional.of(registro));
        ObjectNode entrada = (ObjectNode) mapper.readTree(JSON_REGISTRO);
        entrada.put("rol", "alterado");

        mvc.perform(post("/updateUsuario").contentType(MediaType.APPLICATION_JSON).content(entrada.toString()))
                .andExpect(status().isOk());

        verify(repository).findById(17);
        verify(repository).save(registroCaptor.capture());
        assertEquals(mapper.readTree(JSON_REGISTRO), mapper.valueToTree(registroCaptor.getValue()));
        verifyNoMoreInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"nombre", "correo_electronico", "contrasena"})
    void rechazaAltaSinTextoObligatorio(String campo) throws Exception {
        for (String valor : new String[] {null, "", "   "}) {
            ObjectNode entrada = (ObjectNode) mapper.readTree(JSON_REGISTRO);
            entrada.put(campo, valor);
            mvc.perform(post("/addUsuario").contentType(MediaType.APPLICATION_JSON).content(entrada.toString()))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Estudiante", "Docente", "Otro"})
    void admiteSoloLasCategoriasDelFormulario(String rol) throws Exception {
        ObjectNode entrada = (ObjectNode) mapper.readTree(JSON_REGISTRO);
        entrada.put("rol", rol);
        mvc.perform(post("/addUsuario").contentType(MediaType.APPLICATION_JSON).content(entrada.toString()))
                .andExpect(status().isOk());
        verify(repository).save(registroCaptor.capture());
        assertEquals(rol, registroCaptor.getValue().getRol());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void rechazaUnRolAjenoAlFormulario() throws Exception {
        ObjectNode entrada = (ObjectNode) mapper.readTree(JSON_REGISTRO);
        entrada.put("rol", "Administrador");
        mvc.perform(post("/addUsuario").contentType(MediaType.APPLICATION_JSON).content(entrada.toString()))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @Test
    void asignaEstudianteCuandoNoSeRecibeCategoria() throws Exception {
        ObjectNode entrada = (ObjectNode) mapper.readTree(JSON_REGISTRO);
        entrada.remove("rol");
        mvc.perform(post("/addUsuario").contentType(MediaType.APPLICATION_JSON).content(entrada.toString()))
                .andExpect(status().isOk());
        verify(repository).save(registroCaptor.capture());
        assertEquals("Estudiante", registroCaptor.getValue().getRol());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void rechazaLoginConCredencialesIncompletas() throws Exception {
        for (String campo : List.of("correo_electronico", "contrasena")) {
            ObjectNode entrada = (ObjectNode) mapper.readTree(JSON_REGISTRO);
            entrada.remove(campo);
            mvc.perform(post("/getUsuarioLogin").contentType(MediaType.APPLICATION_JSON).content(entrada.toString()))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(repository);
    }

}

