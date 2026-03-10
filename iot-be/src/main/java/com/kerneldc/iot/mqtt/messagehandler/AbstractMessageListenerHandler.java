package com.kerneldc.iot.mqtt.messagehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kerneldc.iot.mqtt.service.ApplicationContext;
import com.kerneldc.iot.mqtt.service.WebSocketSenderService;
import com.kerneldc.iot.mqtt.topic.TopicHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractMessageListenerHandler implements IMessageListenerHandler {

	protected final ApplicationContext applicationContext;
	protected final ObjectMapper objectMapper;
	protected final WebSocketSenderService webSocketSenderService;
	protected final TopicHelper topicHelper;
	
	protected String lineSeparator = System.getProperty("line.separator");

	protected String addTimeStampToMessage(long timestamp, String message) throws JsonProcessingException {
		var jsonMessage = objectMapper.readTree(message);
		((ObjectNode) jsonMessage).put("timestamp", timestamp);
		return objectMapper.writeValueAsString(jsonMessage);
	}

}
