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
import com.mx.cbtis.gestor.modelo.Comentarios;
import com.mx.cbtis.gestor.repository.ComentariosRepository;

@ExtendWith(MockitoExtension.class)
class ComentariosControllerTest {

    private static final String JSON_REGISTRO = """
            {
                "id_comentario": 17,
                "id_usuario": "Ana",
                "id_tarea": 8,
                "texto_comentario": "Comentario de prueba",
                "fecha_hora": "2026-10-25 12:00:00"
            }
            """;

    @Mock
    private ComentariosRepository repository;

    @InjectMocks
    private ComentariosController controller;

    @Captor
    private ArgumentCaptor<Comentarios> registroCaptor;

    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;
    private Comentarios registro;

    @BeforeEach
    void preparar() {
        registro = new Comentarios(17, "Ana", 8, "Comentario de prueba", "2026-10-25 12:00:00");
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void buscaUnRegistroExistente() throws Exception {
        when(repository.findById(17)).thenReturn(Optional.of(registro));

        mvc.perform(get("/buscarComentario").param("id", "17"))
                .andExpect(status().isOk())
                .andExpect(content().json(JSON_REGISTRO, true));

        verify(repository).findById(17);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void devuelveObjetoVacioCuandoElRegistroNoExiste() throws Exception {
        when(repository.findById(999)).thenReturn(Optional.empty());

        // La API actual responde 200 con los campos a null cuando no encuentra el ID.
        mvc.perform(get("/buscarComentario").param("id", "999"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(new Comentarios()), true));

        verify(repository).findById(999);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void rechazaBusquedaSinIdentificador() throws Exception {
        mvc.perform(get("/buscarComentario"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }

    @Test
    void rechazaIdentificadorQueNoEsUnNumero() throws Exception {
        mvc.perform(get("/buscarComentario").param("id", "abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }

    @Test
    void creaRegistroSinIdentificadorYConservaSusDatos() throws Exception {
        var esperado = mapper.readTree(JSON_REGISTRO);
        ((ObjectNode) esperado).remove("id_comentario");

        mvc.perform(post("/addComentario").contentType(MediaType.APPLICATION_JSON)
                .content(esperado.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(repository).save(registroCaptor.capture());
        var recibido = mapper.valueToTree(registroCaptor.getValue());
        ((ObjectNode) recibido).remove("id_comentario");
        assertEquals(esperado, recibido);
        assertNull(registroCaptor.getValue().getId_comentario());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void actualizaElRegistroConElIdentificadorYDatosRecibidos() throws Exception {
        when(repository.findById(17)).thenReturn(Optional.of(registro));

        mvc.perform(post("/updateComentario").contentType(MediaType.APPLICATION_JSON)
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

        mvc.perform(delete("/deleteComentario").contentType(MediaType.APPLICATION_JSON)
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

        mvc.perform(post("/buscarComentarios"))
                .andExpect(status().isOk())
                .andExpect(content().json("[" + JSON_REGISTRO + "]", true));

        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void devuelveListaVaciaCuandoNoHayRegistros() throws Exception {
        when(repository.findAll()).thenReturn(List.of());

        mvc.perform(post("/buscarComentarios"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]", true));

        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void crearConNullDevuelveCeroSinUsarElRepositorio() {
        assertEquals(0, controller.addComentario(null));
        verifyNoInteractions(repository);
    }

    @Test
    void actualizarConNullDevuelveCeroSinUsarElRepositorio() {
        assertEquals(0, controller.updateComentario(null));
        verifyNoInteractions(repository);
    }

    @Test
    void eliminarConNullDevuelveCeroSinUsarElRepositorio() {
        assertEquals(0, controller.deleteComentario(null));
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @CsvSource({
        "POST, /addComentario",
        "POST, /updateComentario",
        "DELETE, /deleteComentario"
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
    void filtraPorRelacionYDevuelveLosDatos() throws Exception {
        when(repository.findByTarea(8)).thenReturn(List.of(registro));

        mvc.perform(post("/getComentario").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_tarea\": 8}"))
                .andExpect(status().isOk())
                .andExpect(content().json("[" + JSON_REGISTRO + "]", true));

        verify(repository).findByTarea(8);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void devuelveListaVaciaSiLaRelacionNoTieneRegistros() throws Exception {
        when(repository.findByTarea(999)).thenReturn(List.of());

        mvc.perform(post("/getComentario").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_tarea\": 999}"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]", true));

        verify(repository).findByTarea(999);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void rechazaFiltroSinCuerpo() throws Exception {
        mvc.perform(post("/getComentario").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }


    @Test
    void ignoraElIdentificadorEnElAltaParaNoSobrescribirRegistros() throws Exception {
        when(repository.save(any(Comentarios.class))).thenAnswer(invocacion -> {
            Comentarios generado = invocacion.getArgument(0);
            assertNull(generado.getId_comentario(), "El ID enviado no debe llegar al repositorio");
            generado.setId_comentario(42);
            return generado;
        });

        mvc.perform(post("/addComentario").contentType(MediaType.APPLICATION_JSON)
                .content(JSON_REGISTRO))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(repository).save(registroCaptor.capture());
        assertEquals(42, registroCaptor.getValue().getId_comentario());
        verifyNoMoreInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/updateComentario", "/deleteComentario"})
    void rechazaCambiosSinIdValido(String ruta) throws Exception {
        for (String cuerpo : List.of("{}", "{\"id_comentario\":null}",
                "{\"id_comentario\":0}", "{\"id_comentario\":-1}")) {
            mvc.perform(request(ruta.startsWith("/delete") ? HttpMethod.DELETE : HttpMethod.POST, ruta)
                    .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/updateComentario", "/deleteComentario"})
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

        mvc.perform(delete("/deleteComentario").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_comentario\":17}"))
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
        entrada.put("id_usuario", "alterado");
        entrada.put("id_tarea", 999);
        entrada.put("fecha_hora", "alterado");

        mvc.perform(post("/updateComentario").contentType(MediaType.APPLICATION_JSON).content(entrada.toString()))
                .andExpect(status().isOk());

        verify(repository).findById(17);
        verify(repository).save(registroCaptor.capture());
        assertEquals(mapper.readTree(JSON_REGISTRO), mapper.valueToTree(registroCaptor.getValue()));
        verifyNoMoreInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"id_usuario", "texto_comentario"})
    void rechazaAltaSinTextoObligatorio(String campo) throws Exception {
        for (String valor : new String[] {null, "", "   "}) {
            ObjectNode entrada = (ObjectNode) mapper.readTree(JSON_REGISTRO);
            entrada.put(campo, valor);
            mvc.perform(post("/addComentario").contentType(MediaType.APPLICATION_JSON).content(entrada.toString()))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"id_tarea"})
    void rechazaAltaSinRelacionValida(String campo) throws Exception {
        for (Integer valor : new Integer[] {null, 0, -1}) {
            ObjectNode entrada = (ObjectNode) mapper.readTree(JSON_REGISTRO);
            entrada.put(campo, valor);
            mvc.perform(post("/addComentario").contentType(MediaType.APPLICATION_JSON).content(entrada.toString()))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(repository);
    }

    @Test
    void rechazaFiltroSinRelacionValida() throws Exception {
        for (String cuerpo : List.of("{}", "{\"id_tarea\":0}", "{\"id_tarea\":-1}")) {
            mvc.perform(post("/getComentario").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(repository);
    }

}

