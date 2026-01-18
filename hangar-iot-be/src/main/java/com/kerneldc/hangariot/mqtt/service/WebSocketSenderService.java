package com.kerneldc.hangariot.mqtt.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.hangariot.controller.Device;
import com.kerneldc.hangariot.mqtt.topic.TopicHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketSenderService {

	private final SimpMessagingTemplate webSocket;
	private final ApplicationContext applicationContext;
	private final TopicHelper topicHelper;
	private final DeviceService deviceService;

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

	public void publishPowerState(String fullTopic, Object messageObject) {
		String messageString;
		try {
			messageString = objectMapper.writeValueAsString(messageObject);
		} catch (JsonProcessingException e) {
			throw new MessagingException("Error serializing LWT object to string", NestedExceptionUtils.getMostSpecificCause(e));
		}
		var webSocketTopic = websocketTopicsPrefix + "/" + topicHelper.getDevice(fullTopic).getName() + "/power"; 
		webSocket.convertAndSend(webSocketTopic, messageString);
		LOGGER.info("Message [{}] in topic [{}] added to WebSocket topic [{}]", messageString, fullTopic, webSocketTopic);
	}

	public void publishConnectionState(Device device) {
		LOGGER.info("Publishing ConnectionStateMessage message [{}] of device [{}]", applicationContext.getConnectionState(device), device.getName());
		switch (device.getBridge()) {
		case TASMOTA -> {
			var webSocketTopic = websocketTopicsPrefix + "/" + topicHelper.getWsStateTopic(device);
			webSocket.convertAndSend(webSocketTopic, applicationContext.getConnectionState(device));
		}
		case ZIGBEE2MQTT -> {
			var webSocketTopic = websocketTopicsPrefix + "/" + topicHelper.getWsStateTopic(device);
			webSocket.convertAndSend(webSocketTopic, applicationContext.getConnectionState(device));

		}
		}
	}

}
