package com.example.politica_negocio.controller;

import com.example.politica_negocio.model.LogDiagrama;
import com.example.politica_negocio.repository.LogDiagramaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DiagramController {

    private final LogDiagramaRepository logDiagramaRepository;

    /**
     * WebSocket: Recibe actualizaciones del diagrama y las retransmite
     * a todos los usuarios conectados a la misma política.
     */
    @MessageMapping("/diagram/update/{politicaId}")
    @SendTo("/topic/diagram/{politicaId}")
    public Map<String, Object> broadcastDiagramChange(
            @DestinationVariable String politicaId,
            Map<String, Object> diagramData) {
        
        // Persistir como LogDiagrama
        LogDiagrama log = new LogDiagrama();
        log.setTiempo(LocalDateTime.now());
        log.setJson(diagramData);
        log.setCreatedAt(LocalDateTime.now());
        logDiagramaRepository.save(log);
        
        return diagramData;
    }

    /**
     * WebSocket: Broadcast de la posición del cursor de cada usuario.
     * Permite ver en tiempo real dónde está trabajando cada colaborador.
     * Payload: { userId, nombre, x, y, color }
     */
    @MessageMapping("/diagram/cursor/{politicaId}")
    @SendTo("/topic/diagram/cursors/{politicaId}")
    public Map<String, Object> broadcastCursorPosition(
            @DestinationVariable String politicaId,
            Map<String, Object> cursorData) {
        return cursorData;
    }
}
