package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${frontend.url.dev}")
    private String frontendUrl;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(frontendUrl)
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(host)
                .setRelayPort(port)
                .setClientLogin(username)
                .setClientPasscode(password)
                .setSystemLogin(username)
                .setSystemPasscode(password);

        registry.setApplicationDestinationPrefixes("/app");
    }
}
