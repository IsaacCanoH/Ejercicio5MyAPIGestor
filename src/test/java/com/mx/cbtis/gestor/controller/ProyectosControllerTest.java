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
import com.mx.cbtis.gestor.modelo.Proyectos;
import com.mx.cbtis.gestor.repository.ProtectosRepository;

@ExtendWith(MockitoExtension.class)
class ProyectosControllerTest {

    private static final String JSON_REGISTRO = """
            {
                "id_proyecto": 17,
                "nombre": "Proyecto de prueba",
                "descripcion": "Descripcion",
                "fecha_inicio": "2026-10-25",
                "fecha_finalizacion": "2026-10-30",
                "estado_proyecto": "En proceso",
                "fondo": "#FFFFFF"
            }
            """;

    @Mock
    private ProtectosRepository repository;

    @InjectMocks
    private ProyectosController controller;

    @Captor
    private ArgumentCaptor<Proyectos> registroCaptor;

    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;
    private Proyectos registro;

    @BeforeEach
    void preparar() {
        registro = new Proyectos(17, "Proyecto de prueba", "Descripcion", "2026-10-25", "2026-10-30", "En proceso", "#FFFFFF");
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void buscaUnRegistroExistente() throws Exception {
        when(repository.findById(17)).thenReturn(Optional.of(registro));

        mvc.perform(get("/buscarProyecto").param("id_proyecto", "17"))
                .andExpect(status().isOk())
                .andExpect(content().json(JSON_REGISTRO, true));

        verify(repository).findById(17);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void devuelveObjetoVacioCuandoElRegistroNoExiste() throws Exception {
        when(repository.findById(999)).thenReturn(Optional.empty());

        // La API actual responde 200 con los campos a null cuando no encuentra el ID.
        mvc.perform(get("/buscarProyecto").param("id_proyecto", "999"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(new Proyectos()), true));

        verify(repository).findById(999);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void rechazaBusquedaSinIdentificador() throws Exception {
        mvc.perform(get("/buscarProyecto"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }

    @Test
    void rechazaIdentificadorQueNoEsUnNumero() throws Exception {
        mvc.perform(get("/buscarProyecto").param("id_proyecto", "abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }

    @Test
    void creaRegistroSinIdentificadorYConservaSusDatos() throws Exception {
        var esperado = mapper.readTree(JSON_REGISTRO);
        ((ObjectNode) esperado).remove("id_proyecto");

        mvc.perform(post("/addProyecto").contentType(MediaType.APPLICATION_JSON)
                .content(esperado.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(repository).save(registroCaptor.capture());
        var recibido = mapper.valueToTree(registroCaptor.getValue());
        ((ObjectNode) recibido).remove("id_proyecto");
        assertEquals(esperado, recibido);
        assertNull(registroCaptor.getValue().getId_proyecto());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void actualizaElRegistroConElIdentificadorYDatosRecibidos() throws Exception {
        when(repository.findById(17)).thenReturn(Optional.of(registro));

        mvc.perform(post("/updateProyecto").contentType(MediaType.APPLICATION_JSON)
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

        mvc.perform(delete("/deleteProyecto").contentType(MediaType.APPLICATION_JSON)
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

        mvc.perform(post("/buscarProyectos"))
                .andExpect(status().isOk())
                .andExpect(content().json("[" + JSON_REGISTRO + "]", true));

        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void devuelveListaVaciaCuandoNoHayRegistros() throws Exception {
        when(repository.findAll()).thenReturn(List.of());

        mvc.perform(post("/buscarProyectos"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]", true));

        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void crearConNullDevuelveCeroSinUsarElRepositorio() {
        assertEquals(0, controller.addProyecto(null));
        verifyNoInteractions(repository);
    }

    @Test
    void actualizarConNullDevuelveCeroSinUsarElRepositorio() {
        assertEquals(0, controller.updateProyecto(null));
        verifyNoInteractions(repository);
    }

    @Test
    void eliminarConNullDevuelveCeroSinUsarElRepositorio() {
        assertEquals(0, controller.deleteProyecto(null));
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @CsvSource({
        "POST, /addProyecto",
        "POST, /updateProyecto",
        "DELETE, /deleteProyecto"
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
    void ignoraElIdentificadorEnElAltaParaNoSobrescribirRegistros() throws Exception {
        when(repository.save(any(Proyectos.class))).thenAnswer(invocacion -> {
            Proyectos generado = invocacion.getArgument(0);
            assertNull(generado.getId_proyecto(), "El ID enviado no debe llegar al repositorio");
            generado.setId_proyecto(42);
            return generado;
        });

        mvc.perform(post("/addProyecto").contentType(MediaType.APPLICATION_JSON)
                .content(JSON_REGISTRO))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(repository).save(registroCaptor.capture());
        assertEquals(42, registroCaptor.getValue().getId_proyecto());
        verifyNoMoreInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/updateProyecto", "/deleteProyecto"})
    void rechazaCambiosSinIdValido(String ruta) throws Exception {
        for (String cuerpo : List.of("{}", "{\"id_proyecto\":null}",
                "{\"id_proyecto\":0}", "{\"id_proyecto\":-1}")) {
            mvc.perform(request(ruta.startsWith("/delete") ? HttpMethod.DELETE : HttpMethod.POST, ruta)
                    .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/updateProyecto", "/deleteProyecto"})
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

        mvc.perform(delete("/deleteProyecto").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_proyecto\":17}"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(repository).findById(17);
        verify(repository).delete(registro);
        verifyNoMoreInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"nombre"})
    void rechazaAltaSinTextoObligatorio(String campo) throws Exception {
        for (String valor : new String[] {null, "", "   "}) {
            ObjectNode entrada = (ObjectNode) mapper.readTree(JSON_REGISTRO);
            entrada.put(campo, valor);
            mvc.perform(post("/addProyecto").contentType(MediaType.APPLICATION_JSON).content(entrada.toString()))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(repository);
    }

}

