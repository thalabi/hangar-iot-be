package com.kerneldc.iot.springconfig;

import java.util.UUID;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;

import com.kerneldc.iot.mqtt.service.MessageListenerRouter;
import com.kerneldc.iot.mqtt.topic.TopicHelper;

@Configuration
@IntegrationComponentScan
public class MqttConfig {

	@Value("${mqtt.hostname}")
	protected String BROKER_HOST;
	@Value("${mqtt.port:1883}")
	protected String BROKER_PORT;
	@Value("${mqtt.username}")
	protected String USERNAME;
	@Value("${mqtt.password}")
	protected String PASSWORD;

	private static final String LISTENER_CLIENT_NAME = "Hangar-IOT-Controller:" + "listener" + "-";
	private static final String RECEIVER_CLIENT_NAME = "Hangar-IOT-Controller:" + "receiver" + "-";
	private final String listenerClientId = LISTENER_CLIENT_NAME + UUID.randomUUID();
	private final String receiverClientId = RECEIVER_CLIENT_NAME + UUID.randomUUID();

	
	@Bean
    public MqttPahoClientFactory mqttClientFactory() {
        var factory = new DefaultMqttPahoClientFactory();
        var options = new MqttConnectOptions();
        options.setServerURIs(new String[] { "tcp://"+BROKER_HOST+":"+BROKER_PORT});
        options.setUserName(USERNAME);
        options.setPassword(PASSWORD.toCharArray());

		options.setAutomaticReconnect(true);

        factory.setConnectionOptions(options);
        return factory;
    }
	
	// Begin - inbound
	@Bean
    public MessageChannel mqttInboundChannel() {
        return new DirectChannel();
    }

	@Bean
    public MqttPahoMessageDrivenChannelAdapter inbound(TopicHelper topicHelper) {
        var adapter = new MqttPahoMessageDrivenChannelAdapter(listenerClientId, mqttClientFactory());
        adapter.setCompletionTimeout(10000); // in milliseconds
        adapter.setConverter(new DefaultPahoMessageConverter());
        //adapter.setQos(1);
        adapter.setOutputChannel(mqttInboundChannel());
        adapter.setAutoStartup(false);
        return adapter;
    }
	
//	@Bean
//    @ServiceActivator(inputChannel = "mqttInboundChannel")
//    public MessageHandler messageHandler(MessageListenerHandlerService messageListenerHandlerService) {
//		return messageListenerHandlerService;
//    }
	@Bean
    @ServiceActivator(inputChannel = "mqttInboundChannel")
    public MessageHandler messageHandler(MessageListenerRouter messageListenerRouter) {
		return messageListenerRouter;
    }
	// End - inbound
	
	// Begin - outbound
	@Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

	@Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        var messageHandler = new MqttPahoMessageHandler(receiverClientId, mqttClientFactory());
        messageHandler.setAsync(true);
        return messageHandler;
    }
	
	@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
    public interface MqqtGateway {

        void sendMessage(@Header(MqttHeaders.TOPIC) String topic, String message);

    }
	// End - outbound
}
