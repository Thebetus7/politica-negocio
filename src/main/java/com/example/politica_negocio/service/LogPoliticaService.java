package com.example.politica_negocio.service;

import com.example.politica_negocio.dto.LogPoliticaCompileResult;
import com.example.politica_negocio.model.Actividad;
import com.example.politica_negocio.model.Flujo;
import com.example.politica_negocio.model.FormUpdate;
import com.example.politica_negocio.model.LogPolitica;
import com.example.politica_negocio.repository.ActividadRepository;
import com.example.politica_negocio.repository.FlujoRepository;
import com.example.politica_negocio.repository.FormUpdateRepository;
import com.example.politica_negocio.repository.LogPoliticaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogPoliticaService {

    private final ActividadRepository actividadRepository;
    private final FlujoRepository flujoRepository;
    private final FormUpdateRepository formUpdateRepository;
    private final LogPoliticaRepository logPoliticaRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Async
    public void compileAndSaveIfValidAsync(String politicaId) {
        try {
            compileAndSaveIfValid(politicaId);
        } catch (Exception e) {
            log.warn("Error compilando LogPolitica para politica {}: {}", politicaId, e.getMessage());
        }
    }

    public LogPoliticaCompileResult compileAndSaveIfValid(String politicaId) {
        List<Actividad> actividades = actividadRepository.findByPoliticaIdAndDeletedAtIsNull(politicaId);
        List<Flujo> flujos = flujoRepository.findPlantillasByPoliticaId(politicaId);

        Map<String, Actividad> actividadById = actividades.stream()
                .filter(a -> a.getId() != null)
                .collect(Collectors.toMap(Actividad::getId, a -> a, (a, b) -> a));

        Map<String, List<EdgeRef>> outgoingBySource = buildOutgoingIndex(flujos, actividadById.keySet());

        List<Actividad> inicioNodes = actividades.stream()
                .filter(a -> "inicio".equalsIgnoreCase(normalizeStoredTipo(a.getTipoNodo())))
                .toList();

        if (inicioNodes.isEmpty()) {
            return invalidResult("Debe existir exactamente un nodo Inicio");
        }
        if (inicioNodes.size() > 1) {
            return invalidResult("Debe existir exactamente un nodo Inicio");
        }

        Actividad inicio = inicioNodes.get(0);
        String validationError = validateFlow(inicio.getId(), actividadById, outgoingBySource);
        if (validationError != null) {
            return invalidResult(validationError);
        }

        Set<String> reachable = collectReachable(inicio.getId(), outgoingBySource);
        Map<String, String> formularioByActividad = loadFormularioIds(reachable);

        int nextVersion = logPoliticaRepository.findByPoliticaIdAndDeletedAtIsNullOrderByVersionDesc(politicaId)
                .stream()
                .mapToInt(LogPolitica::getVersion)
                .max()
                .orElse(0) + 1;

        Map<String, Object> flujoJson = buildFlujoJson(
                politicaId, inicio.getId(), nextVersion, reachable, actividadById, outgoingBySource, formularioByActividad);

        deactivatePreviousFunctionalVersions(politicaId, "Reemplazado por versión " + nextVersion);

        LogPolitica log = new LogPolitica();
        log.setPoliticaId(politicaId);
        log.setVersion(nextVersion);
        log.setTiempo(LocalDateTime.now());
        log.setValido(true);
        log.setFuncional(true);
        log.setFlujoJson(flujoJson);
        log.setMensajeValidacion(null);
        log.setCreatedAt(LocalDateTime.now());
        logPoliticaRepository.save(log);

        return LogPoliticaCompileResult.builder()
                .valido(true)
                .version(nextVersion)
                .mensaje("Flujo compilado correctamente")
                .flujoJson(flujoJson)
                .build();
    }

    public List<LogPolitica> getHistorial(String politicaId) {
        return logPoliticaRepository.findByPoliticaIdAndDeletedAtIsNullOrderByVersionDesc(politicaId);
    }

    public Optional<LogPolitica> getUltimoValido(String politicaId) {
        return logPoliticaRepository.findFirstByPoliticaIdAndValidoTrueAndFuncionalTrueAndDeletedAtIsNull(politicaId);
    }

    private LogPoliticaCompileResult invalidResult(String mensaje) {
        return LogPoliticaCompileResult.builder()
                .valido(false)
                .mensaje(mensaje)
                .build();
    }

    /** Solo al guardar una nueva versión válida: la anterior deja de ser la activa. */
    private void deactivatePreviousFunctionalVersions(String politicaId, String reason) {
        List<LogPolitica> active = logPoliticaRepository.findByPoliticaIdAndFuncionalTrueAndDeletedAtIsNull(politicaId);
        for (LogPolitica lp : active) {
            lp.setFuncional(false);
            lp.setMensajeValidacion(reason);
            lp.setUpdatedAt(LocalDateTime.now());
            logPoliticaRepository.save(lp);
        }
    }

    private Map<String, List<EdgeRef>> buildOutgoingIndex(List<Flujo> flujos, Set<String> activeActividadIds) {
        Map<String, List<EdgeRef>> map = new HashMap<>();
        for (Flujo f : flujos) {
            if (f.getActividadId() == null || f.getProceso() == null) continue;
            if (!activeActividadIds.contains(f.getActividadId())) continue;
            Object siguientesObj = f.getProceso().get("siguientes");
            if (!(siguientesObj instanceof List<?> list)) continue;
            for (Object item : list) {
                if (!(item instanceof Map<?, ?> sig)) continue;
                Object destId = sig.get("actividadDestinoId");
                if (destId == null) continue;
                if (!activeActividadIds.contains(String.valueOf(destId))) continue;
                String label = sig.get("label") != null ? String.valueOf(sig.get("label")) : "";
                String condicion = sig.get("condicion") != null ? String.valueOf(sig.get("condicion")) : "";
                if (condicion.isEmpty() && f.getProceso().get("condicion") != null) {
                    condicion = String.valueOf(f.getProceso().get("condicion"));
                }
                map.computeIfAbsent(f.getActividadId(), k -> new ArrayList<>())
                        .add(new EdgeRef(String.valueOf(destId), label, condicion, f.getProceso()));
            }
        }
        return map;
    }

    private String validateFlow(
            String inicioId,
            Map<String, Actividad> actividadById,
            Map<String, List<EdgeRef>> outgoingBySource) {

        List<String> finIds = actividadById.values().stream()
                .filter(a -> "fin".equalsIgnoreCase(normalizeStoredTipo(a.getTipoNodo())))
                .map(Actividad::getId)
                .filter(Objects::nonNull)
                .toList();

        if (finIds.isEmpty()) {
            return "Debe existir al menos un nodo Fin en el diagrama";
        }

        Set<String> canEventuallyReachFin = computeNodesThatCanReachFin(finIds, outgoingBySource);

        Set<String> reachableFromInicio = collectReachable(inicioId, outgoingBySource);
        if (!reachableFromInicio.contains(inicioId)) {
            return "El nodo Inicio no es alcanzable en el grafo";
        }

        boolean finReachableForward = finIds.stream().anyMatch(reachableFromInicio::contains);
        if (!finReachableForward) {
            return "El nodo Fin no está conectado al flujo desde Inicio";
        }

        for (String nodeId : reachableFromInicio) {
            Actividad act = actividadById.get(nodeId);
            if (act == null) continue;
            String uiTipo = normalizeStoredTipo(act.getTipoNodo());

            if ("fin".equals(uiTipo)) continue;

            if (!canEventuallyReachFin.contains(nodeId)) {
                return "El nodo '" + act.getNombre() + "' no tiene camino que termine en Fin";
            }

            List<EdgeRef> edges = outgoingBySource.getOrDefault(nodeId, List.of());
            if (edges.isEmpty()) {
                return "El nodo '" + act.getNombre() + "' no tiene conexión de salida hacia Fin";
            }

            if ("decision".equals(uiTipo)) {
                if (edges.size() != 2) {
                    return "La decisión '" + act.getNombre() + "' debe tener exactamente 2 conexiones (Sí/No)";
                }
                boolean missingLabel = edges.stream().anyMatch(e -> e.label == null || e.label.isBlank());
                if (missingLabel) {
                    return "La decisión '" + act.getNombre() + "' requiere etiqueta en cada rama";
                }
                for (EdgeRef edge : edges) {
                    if (!canEventuallyReachFin.contains(edge.targetId)
                            && !isFinNode(edge.targetId, actividadById)) {
                        return "La rama '" + edge.label + "' de la decisión '" + act.getNombre()
                                + "' no termina en Fin";
                    }
                }
            }

            if ("pregunta".equals(uiTipo)) {
                Map<String, Object> meta = parseEstadoJson(act.getEstado());
                String iterativo = meta.get("iterativoTipo") != null
                        ? String.valueOf(meta.get("iterativoTipo"))
                        : "while_do";
                if (!"while_do".equals(iterativo) && !"do_while".equals(iterativo)) {
                    return "La pregunta '" + act.getNombre() + "' debe configurarse como while_do o do_while";
                }
                boolean hasExitToFin = edges.stream().anyMatch(e ->
                        canEventuallyReachFin.contains(e.targetId) || isFinNode(e.targetId, actividadById));
                if (!hasExitToFin) {
                    return "La pregunta '" + act.getNombre() + "' debe tener al menos una salida que llegue a Fin";
                }
            }

            for (EdgeRef edge : edges) {
                if (!canEventuallyReachFin.contains(edge.targetId)
                        && !isFinNode(edge.targetId, actividadById)) {
                    return "La conexión desde '" + act.getNombre() + "' no termina en Fin";
                }
            }
        }

        if (!canEventuallyReachFin.contains(inicioId)) {
            return "No todas las ramas del flujo terminan en un nodo Fin";
        }

        return null;
    }

    /**
     * Nodos que pueden alcanzar algún Fin (recorrido inverso del grafo).
     * Permite ciclos de pregunta si existe al menos una arista hacia un nodo que eventualmente llega a Fin.
     */
    private Set<String> computeNodesThatCanReachFin(
            List<String> finIds,
            Map<String, List<EdgeRef>> outgoingBySource) {

        Map<String, List<String>> incomingByTarget = new HashMap<>();
        for (Map.Entry<String, List<EdgeRef>> entry : outgoingBySource.entrySet()) {
            String source = entry.getKey();
            for (EdgeRef edge : entry.getValue()) {
                incomingByTarget.computeIfAbsent(edge.targetId, k -> new ArrayList<>()).add(source);
            }
        }

        Set<String> canReach = new HashSet<>(finIds);
        Deque<String> queue = new ArrayDeque<>(finIds);
        while (!queue.isEmpty()) {
            String nodeId = queue.poll();
            for (String pred : incomingByTarget.getOrDefault(nodeId, List.of())) {
                if (canReach.add(pred)) {
                    queue.add(pred);
                }
            }
        }
        return canReach;
    }

    private boolean isFinNode(String nodeId, Map<String, Actividad> actividadById) {
        Actividad act = actividadById.get(nodeId);
        return act != null && "fin".equalsIgnoreCase(normalizeStoredTipo(act.getTipoNodo()));
    }

    private Set<String> collectReachable(String startId, Map<String, List<EdgeRef>> outgoingBySource) {
        Set<String> visited = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(startId);
        while (!queue.isEmpty()) {
            String id = queue.poll();
            if (!visited.add(id)) continue;
            for (EdgeRef e : outgoingBySource.getOrDefault(id, List.of())) {
                queue.add(e.targetId);
            }
        }
        return visited;
    }

    private Map<String, String> loadFormularioIds(Set<String> actividadIds) {
        Map<String, String> map = new HashMap<>();
        for (String actId : actividadIds) {
            List<FormUpdate> updates = formUpdateRepository.findByActividadId(actId);
            if (!updates.isEmpty() && updates.get(0).getFormularioId() != null) {
                map.put(actId, updates.get(0).getFormularioId());
            }
        }
        return map;
    }

    private Map<String, Object> buildFlujoJson(
            String politicaId,
            String inicioNodoId,
            int version,
            Set<String> reachable,
            Map<String, Actividad> actividadById,
            Map<String, List<EdgeRef>> outgoingBySource,
            Map<String, String> formularioByActividad) {

        List<Map<String, Object>> nodos = new ArrayList<>();
        List<String> sortedIds = reachable.stream().sorted().toList();

        for (String nodeId : sortedIds) {
            Actividad act = actividadById.get(nodeId);
            if (act == null) continue;

            String uiTipo = normalizeStoredTipo(act.getTipoNodo());
            Map<String, Object> nodo = new LinkedHashMap<>();
            nodo.put("nodoId", nodeId);
            nodo.put("tipo", uiTipo);
            nodo.put("departamentoId", act.getDepartamentoId());

            if (!"inicio".equals(uiTipo) && !"fin".equals(uiTipo)) {
                nodo.put("nombre", act.getNombre());
            }

            if ("actividad".equals(uiTipo)) {
                nodo.put("formularioId", formularioByActividad.getOrDefault(nodeId, null));
            }

            Map<String, Object> meta = parseEstadoJson(act.getEstado());
            if ("decision".equals(uiTipo) || "pregunta".equals(uiTipo)) {
                Object cond = meta.get("condicion");
                if (cond != null && !String.valueOf(cond).isBlank()) {
                    nodo.put("condicion", String.valueOf(cond));
                } else if (act.getNombre() != null) {
                    nodo.put("condicion", act.getNombre());
                }
            }
            if ("pregunta".equals(uiTipo)) {
                String iterativo = meta.get("iterativoTipo") != null
                        ? String.valueOf(meta.get("iterativoTipo"))
                        : "while_do";
                nodo.put("iterativoTipo", iterativo);
            }

            if (!"fin".equals(uiTipo)) {
                List<EdgeRef> edges = outgoingBySource.getOrDefault(nodeId, List.of());
                if (!edges.isEmpty()) {
                    String flujoTipo = inferFlujoTipo(uiTipo, edges.size());
                    List<Map<String, Object>> destinos = edges.stream()
                            .map(e -> {
                                Map<String, Object> d = new LinkedHashMap<>();
                                d.put("nodoId", e.targetId);
                                if (e.label != null && !e.label.isBlank()) d.put("label", e.label);
                                if (e.condicion != null && !e.condicion.isBlank()) d.put("condicion", e.condicion);
                                return d;
                            })
                            .toList();
                    Map<String, Object> siguiente = new LinkedHashMap<>();
                    siguiente.put("flujoTipo", flujoTipo);
                    siguiente.put("destinos", destinos);
                    nodo.put("siguiente", siguiente);
                }
            }

            nodos.add(nodo);
        }

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("politicaId", politicaId);
        root.put("version", version);
        root.put("valido", true);
        root.put("inicioNodoId", inicioNodoId);
        root.put("nodos", nodos);
        return root;
    }

    private String inferFlujoTipo(String uiTipo, int outgoingCount) {
        if ("decision".equals(uiTipo)) return "alternativo";
        if ("pregunta".equals(uiTipo)) return "iterativo";
        if (outgoingCount >= 2) return "paralelo";
        return "secuencial";
    }

    private String normalizeStoredTipo(String tipoNodo) {
        if (tipoNodo == null || tipoNodo.isBlank()) return "actividad";
        String t = tipoNodo.toLowerCase().trim();
        if ("while_do".equals(t) || "do_while".equals(t)) return "pregunta";
        if ("inicio".equals(t)) return "inicio";
        if ("fin".equals(t)) return "fin";
        if ("decision".equals(t)) return "decision";
        return t.replace(' ', '_');
    }

    private Map<String, Object> parseEstadoJson(String estado) {
        if (estado == null || estado.isBlank()) return new HashMap<>();
        String trimmed = estado.trim();
        if (!trimmed.startsWith("{")) return new HashMap<>();
        try {
            return objectMapper.readValue(trimmed, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private record EdgeRef(String targetId, String label, String condicion, Map<String, Object> proceso) {}
}
