package com.example.politica_negocio.service;

import com.example.politica_negocio.dto.CompletarActividadRequest;
import com.example.politica_negocio.dto.GeminiEvaluateRequest;
import com.example.politica_negocio.dto.GeminiEvaluateResponse;
import com.example.politica_negocio.dto.TareaFuncionarioDto;
import com.example.politica_negocio.dto.TramitePasoDto;
import com.example.politica_negocio.dto.TramiteProgresoDto;
import com.example.politica_negocio.model.Actividad;
import com.example.politica_negocio.model.Flujo;
import com.example.politica_negocio.model.FormUpdate;
import com.example.politica_negocio.model.LogPolitica;
import com.example.politica_negocio.model.Portafolio;
import com.example.politica_negocio.repository.ActividadRepository;
import com.example.politica_negocio.repository.FlujoRepository;
import com.example.politica_negocio.repository.FormularioRepository;
import com.example.politica_negocio.repository.FormUpdateRepository;
import com.example.politica_negocio.repository.PortafolioRepository;
import com.example.politica_negocio.repository.PoliticaNegocioRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TramiteEngineService {

    private static final int MAX_AUTO_STEPS = 50;

    private final FlujoRepository flujoRepository;
    private final FormUpdateRepository formUpdateRepository;
    private final ActividadRepository actividadRepository;
    private final PortafolioRepository portafolioRepository;
    private final PoliticaNegocioRepository politicaNegocioRepository;
    private final FuncionarioDepaService funcionarioDepaService;
    private final SimpMessagingTemplate messagingTemplate;
    private final GeminiAiClient geminiAiClient;
    private final LogPoliticaService logPoliticaService;
    private final WorkflowBranchService workflowBranchService;
    private final DocumentoService documentoService;
    private final FormularioRepository formularioRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TramiteProgresoDto completarActividad(String portafolioId, String actividadId, CompletarActividadRequest request) {
        Portafolio portafolio = requirePortafolio(portafolioId);
        if ("completado".equalsIgnoreCase(portafolio.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El trámite ya está completado");
        }

        Map<String, Actividad> actividadById = loadActividadesById(portafolio.getPoliticaId());
        Actividad actividad = actividadById.get(actividadId);
        if (actividad == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Actividad no encontrada");
        }

        String tipo = normalizeTipo(actividad.getTipoNodo());
        if (!"actividad".equals(tipo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo los funcionarios pueden completar nodos de tipo actividad");
        }

        List<Flujo> instancias = flujoRepository.findInstanciasByPoliticaIdAndPortafolioId(
                portafolio.getPoliticaId(), portafolioId);

        List<Flujo> flujosActuales = instancias.stream()
                .filter(f -> actividadId.equals(f.getActividadId()))
                .filter(f -> "en_progreso".equals(estadoActual(f)))
                .toList();

        if (flujosActuales.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La actividad no está en progreso");
        }

        completarNodoInterno(portafolio, actividad, instancias, request);
        procesarNodosAutomaticos(portafolio.getId());

        Portafolio actualizado = requirePortafolio(portafolioId);
        TramiteProgresoDto progreso = buildProgreso(actualizado);
        emitProgreso(progreso);
        return progreso;
    }

    public TramiteProgresoDto getProgreso(String portafolioId) {
        Portafolio portafolio = requirePortafolio(portafolioId);
        return buildProgreso(portafolio);
    }

    public List<TareaFuncionarioDto> getTareasFuncionario(String userId) {
        Set<String> depaIds = funcionarioDepaService.getByUserId(userId).stream()
                .map(fd -> fd.getDepartamentoId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (depaIds.isEmpty()) {
            return List.of();
        }

        List<TareaFuncionarioDto> tareas = new ArrayList<>();
        for (Portafolio p : portafolioRepository.findAllActive()) {
            if (p.getPoliticaId() == null || "completado".equalsIgnoreCase(p.getEstado())) continue;

            Map<String, Actividad> actividadById = loadActividadesById(p.getPoliticaId());
            List<Flujo> instancias = flujoRepository.findInstanciasByPoliticaIdAndPortafolioId(
                    p.getPoliticaId(), p.getId());

            for (Flujo f : instancias) {
                if (!"en_progreso".equals(estadoActual(f))) continue;
                Actividad act = actividadById.get(f.getActividadId());
                if (act == null) continue;
                if (act.getDepartamentoId() == null || !depaIds.contains(act.getDepartamentoId())) continue;

                String tipo = normalizeTipo(act.getTipoNodo());
                if (!"actividad".equals(tipo)) continue;

                tareas.add(TareaFuncionarioDto.builder()
                        .portafolioId(p.getId())
                        .politicaId(p.getPoliticaId())
                        .actividadId(act.getId())
                        .actividadNombre(act.getNombre())
                        .tipoNodo(tipo)
                        .departamentoId(act.getDepartamentoId())
                        .formularioId(resolveFormularioId(act.getId()))
                        .flujoInstanciaId(f.getId())
                        .portafolioJson(p.getJson())
                        .build());
            }
        }
        return tareas;
    }

    /** Arranca el flujo desde el nodo Inicio hacia la primera actividad real. */
    public void iniciarFlujoDesdeInicio(Portafolio portafolio) {
        if (portafolio.getPoliticaId() == null) return;

        Map<String, Actividad> actividadById = loadActividadesById(portafolio.getPoliticaId());
        List<Flujo> instancias = flujoRepository.findInstanciasByPoliticaIdAndPortafolioId(
                portafolio.getPoliticaId(), portafolio.getId());

        Optional<Actividad> inicioOpt = actividadById.values().stream()
                .filter(a -> "inicio".equals(normalizeTipo(a.getTipoNodo())))
                .findFirst();

        if (inicioOpt.isEmpty()) {
            log.warn("Política {} sin nodo inicio; se mantiene arranque legacy por orden", portafolio.getPoliticaId());
            legacyStartByOrden(instancias);
            return;
        }

        String inicioId = inicioOpt.get().getId();
        for (Flujo f : instancias) {
            setEstado(f, "pendiente");
            flujoRepository.save(f);
        }

        List<Flujo> desdeInicio = instancias.stream()
                .filter(f -> inicioId.equals(f.getActividadId()))
                .toList();

        for (Flujo f : desdeInicio) {
            setEstado(f, "completado");
            flujoRepository.save(f);
        }

        List<String> destinos = new ArrayList<>();
        for (Flujo f : desdeInicio) {
            destinos.addAll(extractDestinos(f));
        }

        for (String destinoId : destinos) {
            activateNode(portafolio, destinoId, actividadById, instancias);
        }

        procesarNodosAutomaticos(portafolio.getId());
        emitProgreso(buildProgreso(requirePortafolio(portafolio.getId())));
    }

    /**
     * Auto-completa nodos decision/pregunta (IA) e inicio/time_event hasta llegar
     * a una actividad en_progreso, fin o límite de pasos.
     */
    private void procesarNodosAutomaticos(String portafolioId) {
        for (int step = 0; step < MAX_AUTO_STEPS; step++) {
            Portafolio portafolio = requirePortafolio(portafolioId);
            if ("completado".equalsIgnoreCase(portafolio.getEstado())) {
                return;
            }

            Map<String, Actividad> actividadById = loadActividadesById(portafolio.getPoliticaId());
            List<Flujo> instancias = flujoRepository.findInstanciasByPoliticaIdAndPortafolioId(
                    portafolio.getPoliticaId(), portafolioId);

            Optional<Actividad> candidato = instancias.stream()
                    .filter(f -> "en_progreso".equals(estadoActual(f)))
                    .map(f -> actividadById.get(f.getActividadId()))
                    .filter(Objects::nonNull)
                    .filter(a -> isNodoAutomatico(normalizeTipo(a.getTipoNodo())))
                    .findFirst();

            if (candidato.isEmpty()) {
                return;
            }

            Actividad act = candidato.get();
            String tipo = normalizeTipo(act.getTipoNodo());
            CompletarActividadRequest request;

            if (isAutoPassNode(tipo)) {
                request = new CompletarActividadRequest();
            } else {
                request = buildRequestFromGemini(portafolio, act, instancias);
            }

            completarNodoInterno(portafolio, act, instancias, request);
            emitProgreso(buildProgreso(requirePortafolio(portafolioId)));
        }
        log.warn("Se alcanzó el límite de pasos automáticos para portafolio {}", portafolioId);
    }

    private CompletarActividadRequest buildRequestFromGemini(
            Portafolio portafolio, Actividad act, List<Flujo> instancias) {
        String tipo = normalizeTipo(act.getTipoNodo());
        Map<String, Object> meta = parseEstadoJson(act.getEstado());
        Object condObj = meta.get("condicion");
        String condicion = condObj != null ? String.valueOf(condObj) : act.getNombre();
        Map<String, Actividad> actividadById = loadActividadesById(portafolio.getPoliticaId());

        GeminiEvaluateRequest geminiReq = GeminiEvaluateRequest.builder()
                .tipo("decision".equals(tipo) ? "decision" : "pregunta")
                .nombre(act.getNombre() != null ? act.getNombre() : tipo)
                .condicion(condicion)
                .contexto(buildContextoEvaluacion(portafolio, act, actividadById, instancias))
                .build();

        try {
            GeminiEvaluateResponse resp = geminiAiClient.evaluate(geminiReq);
            CompletarActividadRequest completeReq = new CompletarActividadRequest();

            if ("decision".equals(tipo)) {
                String decision = resp.getDecision() != null ? resp.getDecision() : "No";
                completeReq.setDecisionLabel(decision);
                completeReq.setContenidoUpdate(toJson(Map.of(
                        "iaDecision", decision,
                        "razon", resp.getRazon() != null ? resp.getRazon() : "",
                        "evaluadoPor", "gemini"
                )));
            } else {
                boolean cumple = Boolean.TRUE.equals(resp.getCumple());
                completeReq.setContinuarIteracion(!cumple);
                completeReq.setContenidoUpdate(toJson(Map.of(
                        "cumple", cumple,
                        "continuarIteracion", !cumple,
                        "razon", resp.getRazon() != null ? resp.getRazon() : "",
                        "evaluadoPor", "gemini"
                )));
            }
            return completeReq;
        } catch (RestClientException e) {
            log.error("Fallo evaluación Gemini para actividad {}: {}", act.getId(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No se pudo evaluar el nodo con IA: " + e.getMessage());
        }
    }

    private Map<String, Object> buildContextoEvaluacion(
            Portafolio portafolio,
            Actividad nodoActual,
            Map<String, Actividad> actividadById,
            List<Flujo> instancias) {

        Map<String, Object> ctx = new LinkedHashMap<>();
        String prevActividadId = findActividadAnteriorCompletada(nodoActual.getId(), instancias, actividadById);
        if (prevActividadId == null) {
            return ctx;
        }

        Actividad prevAct = actividadById.get(prevActividadId);
        ctx.put("actividadAnteriorId", prevActividadId);
        ctx.put("actividadAnteriorNombre", prevAct != null ? prevAct.getNombre() : prevActividadId);

        List<FormUpdate> updates = formUpdateRepository.findByPortafolioIdAndActividadId(
                portafolio.getId(), prevActividadId);
        if (updates.isEmpty()) {
            ctx.put("formularioLlenado", Map.of());
            return ctx;
        }

        FormUpdate upd = updates.get(0);
        Map<String, Object> formularioLlenado = parseContenidoUpdateMap(upd.getContenidoUpdate());
        enriquecerCamposArchivo(formularioLlenado, upd.getFormularioId());
        ctx.put("formularioLlenado", formularioLlenado);
        return ctx;
    }

    private String findActividadAnteriorCompletada(
            String nodoActualId,
            List<Flujo> instancias,
            Map<String, Actividad> actividadById) {

        Deque<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        for (Flujo f : instancias) {
            if (extractDestinos(f).contains(nodoActualId) && f.getActividadId() != null) {
                queue.add(f.getActividadId());
            }
        }

        while (!queue.isEmpty()) {
            String candidateId = queue.poll();
            if (!visited.add(candidateId)) continue;

            Actividad act = actividadById.get(candidateId);
            if (act == null) continue;

            String tipo = normalizeTipo(act.getTipoNodo());
            if ("actividad".equals(tipo)) {
                boolean completado = instancias.stream()
                        .filter(f -> candidateId.equals(f.getActividadId()))
                        .anyMatch(f -> "completado".equals(estadoActual(f)));
                if (completado) {
                    return candidateId;
                }
            }

            for (Flujo f : instancias) {
                if (extractDestinos(f).contains(candidateId) && f.getActividadId() != null) {
                    queue.add(f.getActividadId());
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseContenidoUpdateMap(String contenidoUpdate) {
        if (contenidoUpdate == null || contenidoUpdate.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(contenidoUpdate, Map.class);
        } catch (Exception e) {
            return Map.of("raw", contenidoUpdate);
        }
    }

    private void enriquecerCamposArchivo(Map<String, Object> formularioLlenado, String formularioId) {
        if (formularioId == null || formularioLlenado.isEmpty()) return;

        formularioRepository.findById(formularioId).ifPresent(form -> {
            Set<String> campoArchivoIds = form.getCampos().stream()
                    .filter(c -> c != null && "archivo".equals(c.getTipo()))
                    .map(c -> String.valueOf(c.getId()))
                    .collect(Collectors.toSet());

            for (String campoId : campoArchivoIds) {
                Object val = formularioLlenado.get(campoId);
                if (val == null) continue;
                String docId = String.valueOf(val);
                documentoService.findById(docId).ifPresent(doc -> {
                    Map<String, Object> meta = new LinkedHashMap<>();
                    meta.put("documentoId", doc.getId());
                    meta.put("nombreOriginal", doc.getNombreOriginal());
                    meta.put("mimeType", doc.getMimeType());
                    formularioLlenado.put(campoId, meta);
                });
            }
        });
    }

    private void completarNodoInterno(
            Portafolio portafolio,
            Actividad actividad,
            List<Flujo> instancias,
            CompletarActividadRequest request) {

        String actividadId = actividad.getId();
        Map<String, Actividad> actividadById = loadActividadesById(portafolio.getPoliticaId());

        List<Flujo> flujosActuales = instancias.stream()
                .filter(f -> actividadId.equals(f.getActividadId()))
                .filter(f -> "en_progreso".equals(estadoActual(f)))
                .toList();

        if (flujosActuales.isEmpty()) {
            return;
        }

        persistFormUpdate(portafolio.getId(), actividadId, request);

        for (Flujo f : flujosActuales) {
            setEstado(f, "completado");
            flujoRepository.save(f);
        }

        List<String> destinos = resolveDestinos(actividad, flujosActuales, request, actividadById);
        for (String destinoId : destinos) {
            activateNode(portafolio, destinoId, actividadById, instancias);
        }
    }

    private void legacyStartByOrden(List<Flujo> instancias) {
        for (Flujo f : instancias) {
            Object ordenObj = f.getProceso() != null ? f.getProceso().get("orden") : null;
            if (ordenObj != null && "1".equals(String.valueOf(ordenObj))) {
                setEstado(f, "en_progreso");
            } else {
                setEstado(f, "pendiente");
            }
            flujoRepository.save(f);
        }
    }

    private void activateNode(
            Portafolio portafolio,
            String actividadId,
            Map<String, Actividad> actividadById,
            List<Flujo> instancias) {

        Actividad act = actividadById.get(actividadId);
        if (act == null) return;

        String tipo = normalizeTipo(act.getTipoNodo());

        if ("fin".equals(tipo)) {
            portafolio.setEstado("completado");
            portafolio.setUpdatedAt(LocalDateTime.now());
            portafolioRepository.save(portafolio);
            for (Flujo f : instancias) {
                if (actividadId.equals(f.getActividadId())) {
                    setEstado(f, "completado");
                    flujoRepository.save(f);
                }
            }
            return;
        }

        if (isAutoPassNode(tipo)) {
            for (Flujo f : instancias) {
                if (actividadId.equals(f.getActividadId())) {
                    setEstado(f, "completado");
                    flujoRepository.save(f);
                }
            }
            List<String> destinos = new ArrayList<>();
            for (Flujo f : instancias) {
                if (actividadId.equals(f.getActividadId())) {
                    destinos.addAll(extractDestinos(f));
                }
            }
            for (String dest : destinos) {
                activateNode(portafolio, dest, actividadById, instancias);
            }
            return;
        }

        for (Flujo f : instancias) {
            if (actividadId.equals(f.getActividadId())) {
                setEstado(f, "en_progreso");
                flujoRepository.save(f);
            }
        }
    }

    private List<String> resolveDestinos(
            Actividad actividad,
            List<Flujo> flujosActuales,
            CompletarActividadRequest request,
            Map<String, Actividad> actividadById) {

        String tipo = normalizeTipo(actividad.getTipoNodo());

        if ("decision".equals(tipo)) {
            String label = request.getDecisionLabel();
            if (label == null || label.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Se requiere decisionLabel (Sí/No)");
            }
            return flujosActuales.stream()
                    .flatMap(f -> extractDestinosConLabel(f, label).stream())
                    .distinct()
                    .toList();
        }

        if ("pregunta".equals(tipo)) {
            Boolean continuar = request.getContinuarIteracion();
            if (Boolean.TRUE.equals(continuar)) {
                Map<String, Object> meta = parseEstadoJson(actividad.getEstado());
                Object retorno = meta.get("retornoActividadId");
                if (retorno != null) {
                    return List.of(String.valueOf(retorno));
                }
            }
            return flujosActuales.stream()
                    .flatMap(f -> extractDestinos(f).stream())
                    .distinct()
                    .toList();
        }

        return flujosActuales.stream()
                .flatMap(f -> extractDestinos(f).stream())
                .distinct()
                .toList();
    }

    private TramiteProgresoDto buildProgreso(Portafolio portafolio) {
        Map<String, Actividad> actividadById = loadActividadesById(portafolio.getPoliticaId());
        List<Flujo> instancias = flujoRepository.findInstanciasByPoliticaIdAndPortafolioId(
                portafolio.getPoliticaId(), portafolio.getId());
        List<FormUpdate> updates = formUpdateRepository.findByPortafolioId(portafolio.getId());
        Map<String, FormUpdate> updateByActividad = updates.stream()
                .filter(u -> u.getActividadId() != null)
                .collect(Collectors.toMap(FormUpdate::getActividadId, u -> u, (a, b) -> b));

        Map<String, String> estadoRuntime = new HashMap<>();
        instancias.forEach(f -> {
            if (f.getActividadId() != null) {
                estadoRuntime.put(f.getActividadId(), estadoActual(f));
            }
        });

        Map<String, String> decisionTomadaByNodo = new HashMap<>();
        for (FormUpdate upd : updates) {
            if (upd.getActividadId() == null) continue;
            Actividad act = actividadById.get(upd.getActividadId());
            if (act == null || !"decision".equals(normalizeTipo(act.getTipoNodo()))) continue;
            String dec = workflowBranchService.extractDecisionTomada(upd.getContenidoUpdate());
            if (dec != null) {
                decisionTomadaByNodo.put(upd.getActividadId(), dec);
            }
        }

        Map<String, Object> flujoJson = logPoliticaService.getUltimoValido(portafolio.getPoliticaId())
                .map(LogPolitica::getFlujoJson)
                .orElse(null);
        Set<String> omitidos = workflowBranchService.computeOmitidos(flujoJson, decisionTomadaByNodo, estadoRuntime);

        List<TramitePasoDto> pasos = new ArrayList<>();

        instancias.sort(Comparator.comparingInt(f -> {
            Object o = f.getProceso() != null ? f.getProceso().get("orden") : null;
            return o instanceof Number n ? n.intValue() : 999;
        }));

        Set<String> seenActividades = new LinkedHashSet<>();
        for (Flujo f : instancias) {
            String actId = f.getActividadId();
            if (actId == null || seenActividades.contains(actId)) continue;
            seenActividades.add(actId);

            Actividad act = actividadById.get(actId);
            if (act == null) continue;

            String tipo = normalizeTipo(act.getTipoNodo());
            if (isExcludedFromHistorial(tipo)) continue;

            String estado = estadoActual(f);
            FormUpdate upd = updateByActividad.get(actId);
            String decisionTomada = workflowBranchService.extractDecisionTomada(
                    upd != null ? upd.getContenidoUpdate() : null);

            String ramaVisual;
            String estadoDisplay = estado;
            if (omitidos.contains(actId)) {
                ramaVisual = "no_tomada";
                estadoDisplay = "omitido";
            } else if ("en_progreso".equals(estado)) {
                ramaVisual = "activa";
            } else if ("completado".equals(estado)) {
                ramaVisual = "tomada";
            } else {
                ramaVisual = "activa";
            }

            pasos.add(TramitePasoDto.builder()
                    .actividadId(actId)
                    .nombre(act.getNombre())
                    .tipoNodo(tipo)
                    .estado(estadoDisplay)
                    .formularioId(upd != null ? upd.getFormularioId() : resolveFormularioId(actId))
                    .contenidoUpdate(upd != null ? upd.getContenidoUpdate() : null)
                    .updatedAt(upd != null ? upd.getUpdatedAt() : null)
                    .decisionTomada(decisionTomada)
                    .ramaVisual(ramaVisual)
                    .build());
        }

        long totalActivos = pasos.stream()
                .filter(p -> !"no_tomada".equals(p.getRamaVisual()))
                .count();
        long completados = pasos.stream()
                .filter(p -> "completado".equals(p.getEstado()) && !"no_tomada".equals(p.getRamaVisual()))
                .count();
        double progreso = totalActivos == 0 ? 0 : (double) completados / totalActivos;

        return TramiteProgresoDto.builder()
                .portafolioId(portafolio.getId())
                .politicaId(portafolio.getPoliticaId())
                .politicaNombre(resolvePoliticaNombre(portafolio.getPoliticaId()))
                .creadorId(portafolio.getCreadorId())
                .estado(portafolio.getEstado())
                .progreso(progreso)
                .pasos(pasos)
                .build();
    }

    private void emitProgreso(TramiteProgresoDto progreso) {
        try {
            messagingTemplate.convertAndSend("/topic/portafolio/" + progreso.getPortafolioId(), progreso);
            Map<String, Object> listEvent = Map.of(
                    "type", "portafolio_updated",
                    "portafolioId", progreso.getPortafolioId(),
                    "progreso", progreso.getProgreso(),
                    "estado", progreso.getEstado() != null ? progreso.getEstado() : ""
            );
            messagingTemplate.convertAndSend("/topic/portafolios", listEvent);
        } catch (Exception e) {
            log.warn("No se pudo emitir progreso por WebSocket: {}", e.getMessage());
        }
    }

    private void persistFormUpdate(String portafolioId, String actividadId, CompletarActividadRequest request) {
        if (request.getContenidoUpdate() == null && request.getFormularioId() == null
                && request.getDecisionLabel() == null && request.getContinuarIteracion() == null) {
            return;
        }
        List<FormUpdate> existing = formUpdateRepository.findByPortafolioIdAndActividadId(portafolioId, actividadId);
        FormUpdate fu;
        if (!existing.isEmpty()) {
            fu = existing.get(0);
            if (request.getContenidoUpdate() != null) {
                fu.setContenidoUpdate(request.getContenidoUpdate());
            }
            if (request.getFormularioId() != null) {
                fu.setFormularioId(request.getFormularioId());
            }
            fu.setUpdatedAt(LocalDateTime.now());
        } else {
            fu = new FormUpdate();
            fu.setPortafolioId(portafolioId);
            fu.setActividadId(actividadId);
            fu.setFormularioId(request.getFormularioId());
            fu.setContenidoUpdate(request.getContenidoUpdate() != null ? request.getContenidoUpdate() : "{}");
            fu.setCreatedAt(LocalDateTime.now());
        }
        FormUpdate saved = formUpdateRepository.save(fu);
        vincularDocumentosDeFormulario(saved);
    }

    private void vincularDocumentosDeFormulario(FormUpdate fu) {
        if (fu.getContenidoUpdate() == null || fu.getFormularioId() == null) return;
        Map<String, Object> valores = parseContenidoUpdateMap(fu.getContenidoUpdate());
        formularioRepository.findById(fu.getFormularioId()).ifPresent(form -> {
            for (var campo : form.getCampos()) {
                if (campo == null || !"archivo".equals(campo.getTipo())) continue;
                String campoId = String.valueOf(campo.getId());
                Object val = valores.get(campoId);
                if (val != null) {
                    documentoService.vincularFormUpdate(String.valueOf(val), fu.getId());
                }
            }
        });
    }

    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String resolveFormularioId(String actividadId) {
        List<FormUpdate> templates = formUpdateRepository.findByActividadId(actividadId);
        return templates.stream()
                .filter(t -> t.getFormularioId() != null)
                .map(FormUpdate::getFormularioId)
                .findFirst()
                .orElse(null);
    }

    private String resolvePoliticaNombre(String politicaId) {
        return politicaNegocioRepository.findById(politicaId)
                .map(p -> p.getNombre() != null ? p.getNombre() : politicaId)
                .orElse(politicaId);
    }

    private Map<String, Actividad> loadActividadesById(String politicaId) {
        return actividadRepository.findByPoliticaIdAndDeletedAtIsNull(politicaId).stream()
                .filter(a -> a.getId() != null)
                .collect(Collectors.toMap(Actividad::getId, a -> a, (a, b) -> a));
    }

    private Portafolio requirePortafolio(String portafolioId) {
        return portafolioRepository.findById(portafolioId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portafolio no encontrado"));
    }

    private List<String> extractDestinos(Flujo flujo) {
        List<String> destinos = new ArrayList<>();
        if (flujo.getProceso() == null) return destinos;
        Object sigObj = flujo.getProceso().get("siguientes");
        if (!(sigObj instanceof List<?> list)) return destinos;
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                Object dest = map.get("actividadDestinoId");
                if (dest != null) destinos.add(String.valueOf(dest));
            }
        }
        return destinos;
    }

    private List<String> extractDestinosConLabel(Flujo flujo, String label) {
        List<String> destinos = new ArrayList<>();
        if (flujo.getProceso() == null) return destinos;
        Object sigObj = flujo.getProceso().get("siguientes");
        if (!(sigObj instanceof List<?> list)) return destinos;
        String normalized = normalizeDecisionLabel(label);
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                Object itemLabel = map.get("label");
                if (itemLabel != null && normalized.equals(normalizeDecisionLabel(String.valueOf(itemLabel)))) {
                    Object dest = map.get("actividadDestinoId");
                    if (dest != null) destinos.add(String.valueOf(dest));
                }
            }
        }
        return destinos;
    }

    private String normalizeDecisionLabel(String label) {
        if (label == null) return "";
        String t = label.trim().toLowerCase();
        if (t.equals("si") || t.equals("sí") || t.equals("yes")) return "sí";
        if (t.equals("no")) return "no";
        return t;
    }

    private String estadoActual(Flujo f) {
        if (f.getProceso() == null) return "pendiente";
        Object e = f.getProceso().get("estadoActual");
        return e != null ? String.valueOf(e) : "pendiente";
    }

    private void setEstado(Flujo f, String estado) {
        if (f.getProceso() == null) {
            f.setProceso(new HashMap<>());
        }
        f.getProceso().put("estadoActual", estado);
        f.setUpdatedAt(LocalDateTime.now());
    }

    private boolean isAutoPassNode(String tipo) {
        return "time_event".equals(tipo) || "inicio".equals(tipo);
    }

    private boolean isNodoAutomatico(String tipo) {
        return isAutoPassNode(tipo) || "decision".equals(tipo) || "pregunta".equals(tipo);
    }

    private boolean isExcludedFromHistorial(String tipo) {
        return "inicio".equals(tipo) || "time_event".equals(tipo);
    }

    private String normalizeTipo(String tipoNodo) {
        if (tipoNodo == null || tipoNodo.isBlank()) return "actividad";
        String t = tipoNodo.toLowerCase().trim();
        if ("while_do".equals(t) || "do_while".equals(t)) return "pregunta";
        return t.replace(' ', '_');
    }

    private Map<String, Object> parseEstadoJson(String estado) {
        if (estado == null || estado.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(estado, new com.fasterxml.jackson.core.type.TypeReference<>() {});
        } catch (Exception e) {
            return Map.of("condicion", estado);
        }
    }
}
