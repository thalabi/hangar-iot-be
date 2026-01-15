package com.kerneldc.hangariot.mqtt.service.handler;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kerneldc.hangariot.mqtt.service.ApplicationCache;
import com.kerneldc.hangariot.mqtt.topic.TopicHelper;
import com.kerneldc.hangariot.mqtt.topic.TopicHelper.TopicSuffixEnum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractMessageListenerHandler implements IMessageListenerHandler {
	
	@Value("${websocket.topics.prefix:/topic}")
	protected String websocketTopicsPrefix;

	protected final ApplicationCache applicationCache;
	protected final ObjectMapper objectMapper;
	protected final SimpMessagingTemplate webSocket;
	protected final TopicHelper topicHelper;
	
	protected String lineSeparator = System.getProperty("line.separator");

	protected boolean isTasmotaTopic(String topic) {
		return topic.startsWith("stat/") || topic.startsWith("tele/");
	}
	protected boolean isZigbee2mqttTopic(String topic) {
		return topic.startsWith("zigbee2mqtt/");
	}
	
	protected TopicSuffixEnum getTopicSuffix(String topic) {
		var pattern = Pattern.compile("^(.+)/(.+)/(.+)$");
		var matcher = pattern.matcher(topic);
		if (! /* not */ matcher.matches()) {
			throw new IllegalArgumentException(String.format("Could not get suffix from %s", topic));
		}
		return TopicSuffixEnum.valueOf(matcher.group(3));
		
	}

	protected String transformMessageToJson(String message, TopicSuffixEnum fieldName) {
		return "{\"" + fieldName +  "\":\"" + message + "\"}";
	}

	protected String addTimeStampToMessage(long timestamp, String message) throws JsonProcessingException {
		var jsonMessage = objectMapper.readTree(message);
		((ObjectNode) jsonMessage).put("timestamp", timestamp);
		return objectMapper.writeValueAsString(jsonMessage);
	}

	protected void publishMessageToWebSocket(String fullTopic, String messageString) {
		var webSocketTopic = websocketTopicsPrefix + "/state-and-telemetry/" + fullTopic;
		webSocket.convertAndSend(webSocketTopic, messageString);
		LOGGER.info("Message [{}],{} in topic [{}] added to WebSocket topic [{}]", messageString, lineSeparator, fullTopic, webSocketTopic);
	}
	protected void publishMessageToWebSocket(String fullTopic, Object messageObject) {
		String messageString;
		try {
			messageString = objectMapper.writeValueAsString(messageObject);
		} catch (JsonProcessingException e) {
			throw new MessagingException("Error serializing LWT object to string", NestedExceptionUtils.getMostSpecificCause(e));
		}
		var webSocketTopic = websocketTopicsPrefix + "/state-and-telemetry/" + fullTopic;
		webSocket.convertAndSend(webSocketTopic, messageString);
		LOGGER.info("Message [{}],{} in topic [{}] added to WebSocket topic [{}]", messageString, lineSeparator, fullTopic, webSocketTopic);
	}
}
