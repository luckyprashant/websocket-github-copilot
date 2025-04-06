package com.websocket.wstutorial;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(final MessageBrokerRegistry config) {
    	config.setApplicationDestinationPrefixes("/app");  //  Prefix for messages handled by @MessageMapping
        config.enableStompBrokerRelay("/topic/", "/queue/") // Enable the broker
                .setRelayHost("localhost") // Replace with your RabbitMQ host
                .setRelayPort(61613)       // Replace with your RabbitMQ Stomp Port
                .setClientLogin("guest")    // Replace with your RabbitMQ username
                .setClientPasscode("guest");   // Replace with your RabbitMQ password
        config.setApplicationDestinationPrefixes("/ws");
    }

    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        registry.addEndpoint("/register-device")
                .setHandshakeHandler(new UserHandshakeHandler())
                .withSockJS();
    }
    
    @Override
    public void configureWebSocketTransport(final WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory((handler) -> new WebSocketConnectionLostHandlerDecorator(handler));
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
    	RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
        rabbitTemplate.setUserCorrelationId(true);
        rabbitTemplate.setReceiveTimeout(40000);
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
