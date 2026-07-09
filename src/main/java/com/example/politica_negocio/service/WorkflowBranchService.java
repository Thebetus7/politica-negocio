package com.example.politica_negocio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Calcula ramas omitidas en el historial del AC según decisiones tomadas por IA.
 */
@Service
@Slf4j
public class WorkflowBranchService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Set<String> computeOmitidos(
            Map<String, Object> flujoJson,
            Map<String, String> decisionTomadaByNodoId,
            Map<String, String> estadoRuntimeByNodoId) {

        Set<String> omitidos = new HashSet<>();
        if (flujoJson == null || decisionTomadaByNodoId.isEmpty()) {
            return omitidos;
        }

        Map<String, Map<String, Object>> nodosById = parseNodos(flujoJson);
        for (Map.Entry<String, String> entry : decisionTomadaByNodoId.entrySet()) {
            String decisionId = entry.getKey();
            String tomada = normalizeLabel(entry.getValue());
            Map<String, Object> nodo = nodosById.get(decisionId);
            if (nodo == null) {
                continue;
            }
            for (Map<String, String> dest : extractDestinosFromNodo(nodo)) {
                String label = normalizeLabel(dest.get("label"));
                String destId = dest.get("nodoId");
                if (destId == null || labelsMatch(label, tomada)) {
                    continue;
                }
                collectReachable(destId, nodosById, omitidos);
            }
        }

        omitidos.removeIf(id -> {
            String est = estadoRuntimeByNodoId.get(id);
            return "completado".equals(est) || "en_progreso".equals(est);
        });
        return omitidos;
    }

    public String extractDecisionTomada(String contenidoUpdate) {
        if (contenidoUpdate == null || contenidoUpdate.isBlank()) {
            return null;
        }
        try {
            Map<String, Object> map = objectMapper.readValue(
                    contenidoUpdate, new com.fasterxml.jackson.core.type.TypeReference<>() {});
            Object ia = map.get("iaDecision");
            if (ia != null) {
                return String.valueOf(ia);
            }
            Object dec = map.get("decision");
            return dec != null ? String.valueOf(dec) : null;
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> parseNodos(Map<String, Object> flujoJson) {
        Map<String, Map<String, Object>> result = new HashMap<>();
        Object raw = flujoJson.get("nodos");
        if (!(raw instanceof List<?> list)) {
            return result;
        }
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                Object id = map.get("nodoId");
                if (id != null) {
                    result.put(String.valueOf(id), (Map<String, Object>) map);
                }
            }
        }
        return result;
    }
    
    private List<Map<String, String>> extractDestinosFromNodo(Map<String, Object> nodo) {
        List<Map<String, String>> destinos = new ArrayList<>();
        Object sigObj = nodo.get("siguiente");
        if (!(sigObj instanceof Map<?, ?> siguiente)) {
            return destinos;
        }
        Object destList = siguiente.get("destinos");
        if (!(destList instanceof List<?> list)) {
            return destinos;
        }
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                Map<String, String> dest = new HashMap<>();
                Object nodoId = map.get("nodoId");
                Object label = map.get("label");
                if (nodoId != null) {
                    dest.put("nodoId", String.valueOf(nodoId));
                }
                dest.put("label", label != null ? String.valueOf(label) : "");
                destinos.add(dest);
            }
        }
        return destinos;
    }

    private void collectReachable(
            String startId,
            Map<String, Map<String, Object>> nodosById,
            Set<String> acc) {
        Deque<String> queue = new ArrayDeque<>();
        queue.add(startId);
        while (!queue.isEmpty()) {
            String id = queue.poll();
            if (!acc.add(id)) {
                continue;
            }
            Map<String, Object> nodo = nodosById.get(id);
            if (nodo == null) {
                continue;
            }
            for (Map<String, String> dest : extractDestinosFromNodo(nodo)) {
                String nextId = dest.get("nodoId");
                if (nextId != null && !acc.contains(nextId)) {
                    queue.add(nextId);
                }
            }
        }
    }

    private boolean labelsMatch(String a, String b) {
        return normalizeLabel(a).equals(normalizeLabel(b));
    }

    private String normalizeLabel(String label) {
        if (label == null) {
            return "";
        }
        String t = label.trim().toLowerCase();
        if (t.equals("si") || t.equals("sí") || t.equals("yes")) {
            return "sí";
        }
        if (t.equals("no")) {
            return "no";
        }
        return t;
    }
}
