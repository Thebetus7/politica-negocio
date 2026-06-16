package com.example.politica_negocio.service;

import com.example.politica_negocio.dto.LogPoliticaCompileResult;
import com.example.politica_negocio.model.Actividad;
import com.example.politica_negocio.model.Flujo;
import com.example.politica_negocio.model.LogPolitica;
import com.example.politica_negocio.repository.ActividadRepository;
import com.example.politica_negocio.repository.FlujoRepository;
import com.example.politica_negocio.repository.FormUpdateRepository;
import com.example.politica_negocio.repository.LogPoliticaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LogPoliticaServiceTest {

    private static final String POLITICA_ID = "pol-1";

    @Mock
    private ActividadRepository actividadRepository;
    @Mock
    private FlujoRepository flujoRepository;
    @Mock
    private FormUpdateRepository formUpdateRepository;
    @Mock
    private LogPoliticaRepository logPoliticaRepository;

    @InjectMocks
    private LogPoliticaService service;

    @BeforeEach
    void setup() {
        when(formUpdateRepository.findByActividadId(any())).thenReturn(List.of());
        when(logPoliticaRepository.findByPoliticaIdAndDeletedAtIsNullOrderByVersionDesc(POLITICA_ID))
                .thenReturn(List.of());
    }

    @Test
    void compile_inicioActividadFin_creaVersionValida() {
        stubLinearFlow("ini", "act1", "fin");

        LogPoliticaCompileResult result = service.compileAndSaveIfValid(POLITICA_ID);

        assertTrue(result.isValido());
        assertEquals(1, result.getVersion());
        verify(logPoliticaRepository).save(any(LogPolitica.class));
    }

    @Test
    void compile_agregarActividadIntermedia_creaNuevaVersion() {
        when(logPoliticaRepository.findByPoliticaIdAndDeletedAtIsNullOrderByVersionDesc(POLITICA_ID))
                .thenReturn(List.of(existingLog(1)));

        stubFlow(
                act("ini", "inicio", "Inicio"),
                act("a1", "actividad", "A1"),
                act("a2", "actividad", "A2"),
                act("fin", "fin", "Fin"),
                flujo("ini", "a1"),
                flujo("a1", "a2"),
                flujo("a2", "fin")
        );

        LogPoliticaCompileResult result = service.compileAndSaveIfValid(POLITICA_ID);

        assertTrue(result.isValido());
        assertEquals(2, result.getVersion());
    }

    @Test
    void compile_sinFin_noInvalidaVersionAnterior() {
        LogPolitica prev = existingLog(1);
        prev.setFuncional(true);
        stubFlow(
                act("ini", "inicio", "Inicio"),
                act("a1", "actividad", "A1"),
                flujo("ini", "a1")
        );

        LogPoliticaCompileResult result = service.compileAndSaveIfValid(POLITICA_ID);

        assertFalse(result.isValido());
        verify(logPoliticaRepository, never()).save(any(LogPolitica.class));
        assertTrue(prev.isFuncional());
    }

    @Test
    void compile_decisionRamaFinYRamaActividades_esValido() {
        stubFlow(
                act("ini", "inicio", "Inicio"),
                act("dec", "decision", "¿Valido?", "{\"condicion\":\"¿Valido?\"}"),
                act("a2", "actividad", "Procesar"),
                act("fin", "fin", "Fin"),
                flujo("ini", "dec"),
                flujoLabeled("dec", "fin", "No"),
                flujoLabeled("dec", "a2", "Si"),
                flujo("a2", "fin")
        );

        LogPoliticaCompileResult result = service.compileAndSaveIfValid(POLITICA_ID);

        assertTrue(result.isValido());
    }

    @Test
    void compile_preguntaConCicloYSalidaAFin_esValido() {
        stubFlow(
                act("ini", "inicio", "Inicio"),
                act("preg", "while_do", "¿Continuar?", "{\"iterativoTipo\":\"while_do\",\"condicion\":\"¿Continuar?\"}"),
                act("body", "actividad", "Cuerpo"),
                act("fin", "fin", "Fin"),
                flujo("ini", "preg"),
                flujo("preg", "body"),
                flujo("body", "preg"),
                flujo("preg", "fin")
        );

        LogPoliticaCompileResult result = service.compileAndSaveIfValid(POLITICA_ID);

        assertTrue(result.isValido());
    }

    @Test
    void compile_preguntaSoloCicloSinFin_esInvalido() {
        stubFlow(
                act("ini", "inicio", "Inicio"),
                act("preg", "while_do", "¿Loop?", "{\"iterativoTipo\":\"while_do\"}"),
                act("body", "actividad", "Cuerpo"),
                act("fin", "fin", "Fin"),
                flujo("ini", "preg"),
                flujo("preg", "body"),
                flujo("body", "preg")
        );

        LogPoliticaCompileResult result = service.compileAndSaveIfValid(POLITICA_ID);

        assertFalse(result.isValido());
        assertNotNull(result.getMensaje());
    }

    @Test
    void compile_ignoraFlujosHuerfanosHaciaActividadEliminada() {
        stubFlow(
                act("ini", "inicio", "Inicio"),
                act("a1", "actividad", "A1"),
                act("a2", "actividad", "A2"),
                act("fin", "fin", "Fin"),
                flujo("ini", "a1"),
                flujo("a1", "deleted-node"),
                flujo("a1", "a2"),
                flujo("a2", "fin")
        );

        LogPoliticaCompileResult result = service.compileAndSaveIfValid(POLITICA_ID);

        assertTrue(result.isValido());
        List<?> nodos = (List<?>) result.getFlujoJson().get("nodos");
        assertTrue(nodos.stream().noneMatch(n -> String.valueOf(n).contains("deleted-node")));
    }

    @Test
    void compile_decisionAmbasRamasFin_esInvalido() {
        stubFlow(
                act("ini", "inicio", "Inicio"),
                act("dec", "decision", "¿Valido?", "{\"condicion\":\"¿Valido?\"}"),
                act("fin", "fin", "Fin"),
                flujo("ini", "dec"),
                flujoLabeled("dec", "fin", "Si"),
                flujoLabeled("dec", "fin", "No")
        );

        LogPoliticaCompileResult result = service.compileAndSaveIfValid(POLITICA_ID);

        assertFalse(result.isValido());
        assertTrue(result.getMensaje().contains("ambas ramas"));
    }

    @Test
    void compile_paraleloConRamaFin_esInvalido() {
        stubFlow(
                act("ini", "inicio", "Inicio"),
                act("a1", "actividad", "A1"),
                act("a2", "actividad", "A2"),
                act("fin", "fin", "Fin"),
                flujo("ini", "a1"),
                flujo("a1", "a2"),
                flujo("a1", "fin"),
                flujo("a2", "fin")
        );

        LogPoliticaCompileResult result = service.compileAndSaveIfValid(POLITICA_ID);

        assertFalse(result.isValido());
        assertTrue(result.getMensaje().contains("paralelas"));
    }

    @Test
    void compile_cicloSinPregunta_esInvalido() {
        stubFlow(
                act("ini", "inicio", "Inicio"),
                act("a1", "actividad", "A1"),
                act("a2", "actividad", "A2"),
                act("a3", "actividad", "A3"),
                act("fin", "fin", "Fin"),
                flujo("ini", "a1"),
                flujo("a1", "a2"),
                flujo("a2", "a3"),
                flujo("a3", "fin"),
                flujo("a2", "a1")
        );

        LogPoliticaCompileResult result = service.compileAndSaveIfValid(POLITICA_ID);

        assertFalse(result.isValido());
        assertTrue(result.getMensaje().contains("Pregunta"));
    }

    @Test
    void compile_mismoGrafoSinCambioPool_noCreaNuevaVersion() {
        Actividad ini = act("ini", "inicio", "Inicio");
        Actividad act1 = act("act1", "actividad", "Actividad");
        Actividad fin = act("fin", "fin", "Fin");
        stubFlow(ini, act1, fin, flujo("ini", "act1"), flujo("act1", "fin"));

        LogPoliticaCompileResult first = service.compileAndSaveIfValid(POLITICA_ID);
        assertTrue(first.isValido());
        assertEquals(1, first.getVersion());

        LogPolitica prev = new LogPolitica();
        prev.setId("log-1");
        prev.setPoliticaId(POLITICA_ID);
        prev.setVersion(1);
        prev.setValido(true);
        prev.setFuncional(true);
        prev.setFlujoJson(first.getFlujoJson());

        when(logPoliticaRepository.findByPoliticaIdAndDeletedAtIsNullOrderByVersionDesc(POLITICA_ID))
                .thenReturn(List.of(prev));
        when(logPoliticaRepository.findFirstByPoliticaIdAndValidoTrueAndFuncionalTrueAndDeletedAtIsNull(POLITICA_ID))
                .thenReturn(Optional.of(prev));

        LogPoliticaCompileResult second = service.compileAndSaveIfValid(POLITICA_ID);

        assertTrue(second.isValido());
        assertEquals(1, second.getVersion());
        verify(logPoliticaRepository, times(1)).save(any(LogPolitica.class));
    }

    @Test
    void compile_cambioDepartamentoNodo_siCreaNuevaVersion() {
        Actividad ini = act("ini", "inicio", "Inicio");
        Actividad act1 = act("act1", "actividad", "Actividad");
        act1.setDepartamentoId("dep-1");
        Actividad fin = act("fin", "fin", "Fin");
        stubFlow(ini, act1, fin, flujo("ini", "act1"), flujo("act1", "fin"));

        LogPoliticaCompileResult first = service.compileAndSaveIfValid(POLITICA_ID);
        assertEquals(1, first.getVersion());

        LogPolitica prev = new LogPolitica();
        prev.setId("log-1");
        prev.setPoliticaId(POLITICA_ID);
        prev.setVersion(1);
        prev.setValido(true);
        prev.setFuncional(true);
        prev.setFlujoJson(first.getFlujoJson());

        act1.setDepartamentoId("dep-2");
        stubFlow(ini, act1, fin, flujo("ini", "act1"), flujo("act1", "fin"));

        when(logPoliticaRepository.findByPoliticaIdAndDeletedAtIsNullOrderByVersionDesc(POLITICA_ID))
                .thenReturn(List.of(prev));
        when(logPoliticaRepository.findFirstByPoliticaIdAndValidoTrueAndFuncionalTrueAndDeletedAtIsNull(POLITICA_ID))
                .thenReturn(Optional.of(prev));
        when(logPoliticaRepository.findByPoliticaIdAndFuncionalTrueAndDeletedAtIsNull(POLITICA_ID))
                .thenReturn(List.of(prev));

        LogPoliticaCompileResult second = service.compileAndSaveIfValid(POLITICA_ID);

        assertTrue(second.isValido());
        assertEquals(2, second.getVersion());
        verify(logPoliticaRepository, atLeast(2)).save(any(LogPolitica.class));
    }

    @Test
    void compile_valido_desactivaVersionAnteriorFuncional() {
        LogPolitica prev = existingLog(1);
        when(logPoliticaRepository.findByPoliticaIdAndFuncionalTrueAndDeletedAtIsNull(POLITICA_ID))
                .thenReturn(List.of(prev));
        when(logPoliticaRepository.findByPoliticaIdAndDeletedAtIsNullOrderByVersionDesc(POLITICA_ID))
                .thenReturn(List.of(prev));

        stubLinearFlow("ini", "act1", "fin");

        service.compileAndSaveIfValid(POLITICA_ID);

        assertFalse(prev.isFuncional());
        ArgumentCaptor<LogPolitica> captor = ArgumentCaptor.forClass(LogPolitica.class);
        verify(logPoliticaRepository, atLeastOnce()).save(captor.capture());
        assertTrue(captor.getAllValues().stream().anyMatch(l -> l.isFuncional() && l.getVersion() == 2));
    }

    private void stubLinearFlow(String ini, String act, String fin) {
        stubFlow(
                act(ini, "inicio", "Inicio"),
                act(act, "actividad", "Actividad"),
                act(fin, "fin", "Fin"),
                flujo(ini, act),
                flujo(act, fin)
        );
    }

    private void stubFlow(Object... items) {
        List<Actividad> actividades = new ArrayList<>();
        List<Flujo> flujos = new ArrayList<>();
        for (Object item : items) {
            if (item instanceof Actividad a) actividades.add(a);
            if (item instanceof Flujo f) flujos.add(f);
        }
        when(actividadRepository.findByPoliticaIdAndDeletedAtIsNull(POLITICA_ID)).thenReturn(actividades);
        when(flujoRepository.findPlantillasByPoliticaId(POLITICA_ID)).thenReturn(flujos);
    }

    private Actividad act(String id, String tipo, String nombre) {
        return act(id, tipo, nombre, "pendiente");
    }

    private Actividad act(String id, String tipo, String nombre, String estado) {
        Actividad a = new Actividad();
        a.setId(id);
        a.setPoliticaId(POLITICA_ID);
        a.setDepartamentoId("dep-1");
        a.setNombre(nombre);
        a.setTipoNodo(tipo);
        a.setEstado(estado);
        a.setEjeX("0");
        a.setEjeY("0");
        return a;
    }

    private Flujo flujo(String from, String to) {
        return flujoLabeled(from, to, "");
    }

    private Flujo flujoLabeled(String from, String to, String label) {
        Flujo f = new Flujo();
        f.setId("fl-" + from + "-" + to);
        f.setPoliticaId(POLITICA_ID);
        f.setActividadId(from);
        Map<String, Object> proceso = new HashMap<>();
        proceso.put("tipo", "secuencial");
        Map<String, Object> sig = new HashMap<>();
        sig.put("actividadDestinoId", to);
        if (label != null && !label.isBlank()) sig.put("label", label);
        proceso.put("siguientes", List.of(sig));
        f.setProceso(proceso);
        return f;
    }

    private LogPolitica existingLog(int version) {
        LogPolitica log = new LogPolitica();
        log.setId("log-" + version);
        log.setPoliticaId(POLITICA_ID);
        log.setVersion(version);
        log.setValido(true);
        log.setFuncional(true);
        log.setFlujoJson(Map.of("version", version));
        return log;
    }
}
