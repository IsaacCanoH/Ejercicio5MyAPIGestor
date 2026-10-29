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
import com.mx.cbtis.gestor.modelo.Asignaciones;
import com.mx.cbtis.gestor.repository.AsignacionesRepository;

@ExtendWith(MockitoExtension.class)
class AsignacionesControllerTest {

    private static final String JSON_REGISTRO = """
            {
                "id_asignacion": 17,
                "id_usuario": 4,
                "id_tarea": 8,
                "fecha_asignacion": "2026-10-25"
            }
            """;

    @Mock
    private AsignacionesRepository repository;

    @InjectMocks
    private AsignacionesController controller;

    @Captor
    private ArgumentCaptor<Asignaciones> registroCaptor;

    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;
    private Asignaciones registro;

    @BeforeEach
    void preparar() {
        registro = new Asignaciones(17, 4, 8, "2026-10-25");
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void buscaUnRegistroExistente() throws Exception {
        when(repository.findById(17)).thenReturn(Optional.of(registro));

        mvc.perform(get("/buscarAsignacion").param("id", "17"))
                .andExpect(status().isOk())
                .andExpect(content().json(JSON_REGISTRO, true));

        verify(repository).findById(17);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void devuelveObjetoVacioCuandoElRegistroNoExiste() throws Exception {
        when(repository.findById(999)).thenReturn(Optional.empty());

        // La API actual responde 200 con los campos a null cuando no encuentra el ID.
        mvc.perform(get("/buscarAsignacion").param("id", "999"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(new Asignaciones()), true));

        verify(repository).findById(999);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void rechazaBusquedaSinIdentificador() throws Exception {
        mvc.perform(get("/buscarAsignacion"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }

    @Test
    void rechazaIdentificadorQueNoEsUnNumero() throws Exception {
        mvc.perform(get("/buscarAsignacion").param("id", "abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }

    @Test
    void creaRegistroSinIdentificadorYConservaSusDatos() throws Exception {
        var esperado = mapper.readTree(JSON_REGISTRO);
        ((ObjectNode) esperado).remove("id_asignacion");

        mvc.perform(post("/addAsignacion").contentType(MediaType.APPLICATION_JSON)
                .content(esperado.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(repository).save(registroCaptor.capture());
        var recibido = mapper.valueToTree(registroCaptor.getValue());
        ((ObjectNode) recibido).remove("id_asignacion");
        assertEquals(esperado, recibido);
        assertNull(registroCaptor.getValue().getId_asignacion());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void actualizaElRegistroConElIdentificadorYDatosRecibidos() throws Exception {
        when(repository.findById(17)).thenReturn(Optional.of(registro));

        mvc.perform(post("/updateAsignacion").contentType(MediaType.APPLICATION_JSON)
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

        mvc.perform(delete("/deleteAsignacion").contentType(MediaType.APPLICATION_JSON)
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

        mvc.perform(post("/buscarAsignaciones"))
                .andExpect(status().isOk())
                .andExpect(content().json("[" + JSON_REGISTRO + "]", true));

        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void devuelveListaVaciaCuandoNoHayRegistros() throws Exception {
        when(repository.findAll()).thenReturn(List.of());

        mvc.perform(post("/buscarAsignaciones"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]", true));

        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void crearConNullDevuelveCeroSinUsarElRepositorio() {
        assertEquals(0, controller.addAsignacion(null));
        verifyNoInteractions(repository);
    }

    @Test
    void actualizarConNullDevuelveCeroSinUsarElRepositorio() {
        assertEquals(0, controller.updateAsignacion(null));
        verifyNoInteractions(repository);
    }

    @Test
    void eliminarConNullDevuelveCeroSinUsarElRepositorio() {
        assertEquals(0, controller.deleteAsignacion(null));
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @CsvSource({
        "POST, /addAsignacion",
        "POST, /updateAsignacion",
        "DELETE, /deleteAsignacion"
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
        when(repository.save(any(Asignaciones.class))).thenAnswer(invocacion -> {
            Asignaciones generado = invocacion.getArgument(0);
            assertNull(generado.getId_asignacion(), "El ID enviado no debe llegar al repositorio");
            generado.setId_asignacion(42);
            return generado;
        });

        mvc.perform(post("/addAsignacion").contentType(MediaType.APPLICATION_JSON)
                .content(JSON_REGISTRO))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(repository).save(registroCaptor.capture());
        assertEquals(42, registroCaptor.getValue().getId_asignacion());
        verifyNoMoreInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/updateAsignacion", "/deleteAsignacion"})
    void rechazaCambiosSinIdValido(String ruta) throws Exception {
        for (String cuerpo : List.of("{}", "{\"id_asignacion\":null}",
                "{\"id_asignacion\":0}", "{\"id_asignacion\":-1}")) {
            mvc.perform(request(ruta.startsWith("/delete") ? HttpMethod.DELETE : HttpMethod.POST, ruta)
                    .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/updateAsignacion", "/deleteAsignacion"})
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

        mvc.perform(delete("/deleteAsignacion").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_asignacion\":17}"))
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
        entrada.put("id_tarea", 999);
        entrada.put("fecha_asignacion", "alterado");

        mvc.perform(post("/updateAsignacion").contentType(MediaType.APPLICATION_JSON).content(entrada.toString()))
                .andExpect(status().isOk());

        verify(repository).findById(17);
        verify(repository).save(registroCaptor.capture());
        assertEquals(mapper.readTree(JSON_REGISTRO), mapper.valueToTree(registroCaptor.getValue()));
        verifyNoMoreInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"id_usuario", "id_tarea"})
    void rechazaAltaSinRelacionValida(String campo) throws Exception {
        for (Integer valor : new Integer[] {null, 0, -1}) {
            ObjectNode entrada = (ObjectNode) mapper.readTree(JSON_REGISTRO);
            entrada.put(campo, valor);
            mvc.perform(post("/addAsignacion").contentType(MediaType.APPLICATION_JSON).content(entrada.toString()))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(repository);
    }

}

