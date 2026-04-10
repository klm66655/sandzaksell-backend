package com.sandzaksell.sandzaksell.config;

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
        config.enableSimpleBroker("/topic", "/queue"); // Kanali za slanje
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Čitamo istu varijablu sa Railway-a
        String allowedOrigins = System.getenv("ALLOWED_ORIGINS");
        String[] origins;

        if (allowedOrigins != null) {
            origins = allowedOrigins.split(",");
        } else {
            // Fallback na localhost ako nema varijable
            origins = new String[]{"http://localhost:5173"};
        }

        registry.addEndpoint("/ws")
                .setAllowedOrigins(origins) // Sada pušta sve što si stavio u Railway!
                .withSockJS();
    }
}