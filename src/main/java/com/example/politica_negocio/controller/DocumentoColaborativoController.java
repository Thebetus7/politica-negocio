package com.example.politica_negocio.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import java.util.Map;

@Controller
public class DocumentoColaborativoController {

    /**
     * WebSocket: Recibe actualizaciones del texto editable del documento
     * y las retransmite a todos los usuarios conectados en la misma sesión.
     * Payload: { id, contenido, usuario }
     */
    @MessageMapping("/documento/update/{documentoId}")
    @SendTo("/topic/documento/{documentoId}")
    public Map<String, Object> broadcastDocumentoChange(
            @DestinationVariable String documentoId,
            Map<String, Object> documentoData) {
        return documentoData;
    }

    /**
     * WebSocket: Broadcast de la posición del cursor / awareness de cada usuario.
     * Payload: { userId, nombre, position, color }
     */
    @MessageMapping("/documento/cursor/{documentoId}")
    @SendTo("/topic/documento/cursors/{documentoId}")
    public Map<String, Object> broadcastCursorPosition(
            @DestinationVariable String documentoId,
            Map<String, Object> cursorData) {
        return cursorData;
    }
}
