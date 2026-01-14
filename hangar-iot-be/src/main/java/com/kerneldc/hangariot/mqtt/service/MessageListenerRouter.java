package com.kerneldc.hangariot.mqtt.service;

import java.util.Collection;

import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import com.kerneldc.hangariot.mqtt.service.handler.IMessageListenerHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Routes messages from mqtt queues to websocket queues
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageListenerRouter implements MessageHandler {

	private final Collection<IMessageListenerHandler> messageListenerHandlerCollection;
	
//	private String lineSeparator = System.getProperty("line.separator");
	
	@Override
	public void handleMessage(Message<?> messageObject) throws MessagingException {
		var fullTopic = (String)messageObject.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
		var timestamp = (long)messageObject.getHeaders().get(MessageHeaders.TIMESTAMP); 
		var message = (String)messageObject.getPayload();
		
//		LOGGER.info("Message [{}]{} arrived in topic [{}]", message, lineSeparator, fullTopic);
		LOGGER.info("Message [{}] arrived in topic [{}] at [{}]", message, fullTopic, timestamp);
			
		for (IMessageListenerHandler handler: messageListenerHandlerCollection) {
			if (handler.canHandleMessage(fullTopic)) {
				LOGGER.info("Routing message to [{}]", handler.getClass().getSimpleName());
				handler.handleMessage(fullTopic, timestamp, message);
			}
		};
	}
}
