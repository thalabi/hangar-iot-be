package com.kerneldc.iot.websocket.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.iot.domain.device.Device;
import com.kerneldc.iot.mqtt.service.ApplicationContext;
import com.kerneldc.iot.mqtt.topic.TopicHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketSenderService {

	private final SimpMessagingTemplate webSocket;
	private final ApplicationContext applicationContext;
	private final TopicHelper topicHelper;

	private final ObjectMapper objectMapper;
	
	@Value("${websocket.topics.prefix:/topic}")
	private String websocketTopicsPrefix;

	public void publishMessageToWebSocket(String fullTopic, String messageString) {
		var webSocketTopic = websocketTopicsPrefix + "/state-and-telemetry/" + fullTopic;
		webSocket.convertAndSend(webSocketTopic, messageString);
		LOGGER.info("Message [{}] in topic [{}] added to WebSocket topic [{}]", messageString, fullTopic, webSocketTopic);
	}

	public void publishMessageToWebSocket(String fullTopic, Object messageObject) {
		String messageString;
		try {
			messageString = objectMapper.writeValueAsString(messageObject);
		} catch (JsonProcessingException e) {
			throw new MessagingException("Error serializing LWT object to string", NestedExceptionUtils.getMostSpecificCause(e));
		}
		var webSocketTopic = websocketTopicsPrefix + "/state-and-telemetry/" + fullTopic;
		webSocket.convertAndSend(webSocketTopic, messageString);
		LOGGER.info("Message [{}] in topic [{}] added to WebSocket topic [{}]", messageString, fullTopic, webSocketTopic);
	}

	public void publishConnectionState(Device device) {
		LOGGER.info("Publishing ConnectionStateMessage message [{}] of device [{}]", applicationContext.getConnectionState(device), device.getName());
		var webSocketTopic = websocketTopicsPrefix + "/" + topicHelper.getWsConnectionStateTopic(device);
		if (applicationContext.getConnectionState(device) == null) {
			LOGGER.error("No connection state found in cache for device [{}]", device.getName());
			return;
		}
		webSocket.convertAndSend(webSocketTopic, applicationContext.getConnectionState(device));
	}

	public void publishPowerState(String fullTopic, Object messageObject) {
		String messageString;
		try {
			messageString = objectMapper.writeValueAsString(messageObject);
		} catch (JsonProcessingException e) {
			throw new MessagingException("Error serializing LWT object to string", NestedExceptionUtils.getMostSpecificCause(e));
		}
		var webSocketTopic = websocketTopicsPrefix + "/" + topicHelper.getDeviceFromTopic(fullTopic).getName() + "/power"; 
		webSocket.convertAndSend(webSocketTopic, messageString);
		LOGGER.info("Message [{}] in topic [{}] added to WebSocket topic [{}]", messageString, fullTopic, webSocketTopic);
	}

	public void publishZigbee2MqttState(String fullTopic, String message) {
		var webSocketTopic = websocketTopicsPrefix + "/" + fullTopic;
//		LOGGER.info("Publishing web socket, message [{}], topic [{}]", message, webSocketTopic);
		webSocket.convertAndSend(webSocketTopic, message);
		LOGGER.info("Message [{}] in topic [{}] added to WebSocket topic [{}]", message, fullTopic, webSocketTopic);
	}

	public void publishZigbee2MqttAttributeChanges(Device device) throws JsonProcessingException {
		var webSocketTopic = websocketTopicsPrefix + "/" + device.getName() + "/attributeChanges";
		var attributeChanges = applicationContext.getAttributeChanges(device);
//		LOGGER.info("Publishing web socket, message [{}], topic [{}]", attributeChanges, webSocketTopic);
		webSocket.convertAndSend(webSocketTopic, attributeChanges);
		LOGGER.info("Message [{}] added to WebSocket topic [{}]", attributeChanges, webSocketTopic);
	}

	public void publishEspresenseState(String fullTopic, String message) {
		var webSocketTopic = websocketTopicsPrefix + "/" + fullTopic;
//		LOGGER.info("Publishing web socket, message [{}], topic [{}]", message, webSocketTopic);
		webSocket.convertAndSend(webSocketTopic, message);
		LOGGER.info("Message [{}] in topic [{}] added to WebSocket topic [{}]", message, fullTopic, webSocketTopic);
	}

	public void publishEspresenseAttributeChanges(Device device) throws JsonProcessingException {
//		var webSocketTopic = websocketTopicsPrefix + "/" + device.getName() + "/attributeChanges";
//		var attributeChanges = applicationContext.getAttributeChanges(device);
////		LOGGER.info("Publishing web socket, message [{}], topic [{}]", attributeChanges, webSocketTopic);
//		webSocket.convertAndSend(webSocketTopic, attributeChanges);
//		LOGGER.info("Message [{}] added to WebSocket topic [{}]", attributeChanges, webSocketTopic);
	}

}
