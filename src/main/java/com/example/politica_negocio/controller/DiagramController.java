package com.example.politica_negocio.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/diagrams")
public class DiagramController {

    // Método que el contexto original (Laravel) pedía. Provee el JSON Base
    @GetMapping("/base/{nombre}")
    public Map<String, Object> getBaseJsonDiagram(@PathVariable String nombre) {
        
        Map<String, Object> diagram = new HashMap<>();
        diagram.put("class", "go.GraphLinksModel");
        
        Object[] nodeDataArray = {
            Map.of("key", "Pool1", "text", nombre, "isGroup", true, "category", "Pool", "color", "white"),
            Map.of("key", "Lane1", "text", "Carril 1", "isGroup", true, "category", "Lane", "group", "Pool1", "color", "lightyellow"),
            Map.of("key", "inicio", "text", "Inicio", "group", "Lane1"),
            Map.of("key", "actividad1", "text", "Primera Actividad", "group", "Lane1")
        };
        
        Object[] linkDataArray = {
            Map.of("from", "inicio", "to", "actividad1")
        };
        
        diagram.put("nodeDataArray", nodeDataArray);
        diagram.put("linkDataArray", linkDataArray);
        
        return diagram; // Spring Boot convierte Map a JSON nativamente vía Jackson
    }

    // WEB SOCKET CONTROLLER LISTENER (STOMP)
    // Recibe mensajes desde /app/diagram/update/{id} y los manda a /topic/diagram/{id}
    @MessageMapping("/diagram/update/{id}")
    @SendTo("/topic/diagram/{id}")
    public String broadcastDiagramChange(String jsonPayload) {
        // Aqui en el futuro se puede inyectar un Repositorio (LogDiagramaRepository)
        // para guardar a Base de Datos en tiempo real de forma transaccional.
        // Por ahora, simplemente retransmite al resto de los clientes.
        return jsonPayload; 
    }
}
