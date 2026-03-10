package com.kerneldc.iot.mqtt.service;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import com.kerneldc.iot.mqtt.messagehandler.IMessageListenerHandler;
import com.kerneldc.iot.mqtt.topic.TopicHelper;

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
	private final TopicHelper topicHelper;
	
	@Override
	public void handleMessage(Message<?> messageObject) throws MessagingException {
//		LOGGER.info("messageObject [{}]", messageObject);
		var fullTopic = (String)messageObject.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
		var timestamp = (long)messageObject.getHeaders().get(MessageHeaders.TIMESTAMP); 
		var message = (String)messageObject.getPayload();
		
		if (topicHelper.isDevicesInfoTopic(fullTopic)) {
			LOGGER.info("Message ([{}] characters) arrived in topic [{}] at [{}]", StringUtils.length(message), fullTopic, timestamp);
		} else {
			LOGGER.info("Message [{}] arrived in topic [{}] at [{}]", message, fullTopic, timestamp);
		}
			
		for (IMessageListenerHandler handler: messageListenerHandlerCollection) {
			if (handler.canHandleMessage(fullTopic)) {
				LOGGER.info("Routing message to [{}]", handler.getClass().getSimpleName());
				handler.handleMessage(fullTopic, timestamp, message);
			}
		};
	}
}
