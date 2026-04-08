package com.example.politica_negocio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita un broker en memoria simple para enviar los updates
        config.enableSimpleBroker("/topic");
        // Prefijo para mensajes enviados desde el cliente
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint al que Angular/Flutter conectará el WebSocket
        registry.addEndpoint("/ws-diagram")
                .setAllowedOriginPatterns("*") // Permisos libres para colaboracion abierta
                .withSockJS();
    }
}
