package com.kerneldc.iot.springconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Value("${websocket.endpoint:/protected/iot-websocket}")
	private String WEBSOCKET_ENDPOINT;
	
	@Value("${websocket.topics.prefix:/topic}")
	private String WEBSOCKET_TOPICS_PREFIX;
	
    @Value("${application.security.corsFilter.corsUrlsToAllow}")
    private String[] corsUrlsToAllow;

	// configure endpoint the client will connect to
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
    	registry.addEndpoint(WEBSOCKET_ENDPOINT).setAllowedOrigins(corsUrlsToAllow); // client will connect to this endpoint
        registry.addEndpoint(WEBSOCKET_ENDPOINT).withSockJS(); // enable SockJS
    }

    // configure in-memory broker with prefix /topic
	@Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        //config.enableSimpleBroker("/topic/state-and-telemetry");
        config.enableSimpleBroker(WEBSOCKET_TOPICS_PREFIX);
        //config.setApplicationDestinationPrefixes("/app"); // we dont have 
	}
}
