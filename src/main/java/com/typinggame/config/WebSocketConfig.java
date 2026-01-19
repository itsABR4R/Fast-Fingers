package com.typinggame.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for STOMP protocol.
 * Enables browser-compatible WebSocket communication while maintaining
 * the existing raw Socket programming for AOOP requirements.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for broadcasting to subscribed clients
        config.enableSimpleBroker("/topic");

        // Set application destination prefix for client messages
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register STOMP endpoint matching frontend expectation
        registry.addEndpoint("/fastfingers-ws")
                .setAllowedOrigins("http://localhost:5173", "http://localhost:3000")
                .withSockJS(); // Enable SockJS fallback for browsers without WebSocket support
    }
}
