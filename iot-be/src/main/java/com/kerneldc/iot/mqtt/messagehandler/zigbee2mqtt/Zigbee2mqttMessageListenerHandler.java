package com.kerneldc.iot.mqtt.messagehandler.zigbee2mqtt;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.iot.mqtt.command.Zigbee2MqttCommandEnum;
import com.kerneldc.iot.mqtt.messagehandler.AbstractMessageListenerHandler;
import com.kerneldc.iot.mqtt.result.zigbee2mqtt.StateResult;
import com.kerneldc.iot.mqtt.service.ApplicationContext;
import com.kerneldc.iot.mqtt.service.WebSocketSenderService;
import com.kerneldc.iot.mqtt.topic.TopicHelper;
import com.kerneldc.iot.websocket.ConnectionStateEnum;
import com.kerneldc.iot.websocket.message.ConnectionStateMessage;
import com.kerneldc.iot.websocket.message.PowerMessage;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class Zigbee2mqttMessageListenerHandler extends AbstractMessageListenerHandler {

	public Zigbee2mqttMessageListenerHandler(ApplicationContext applicationContext, ObjectMapper objectMapper,
			WebSocketSenderService webSocketSenderService, TopicHelper topicHelper) {
		super(applicationContext, objectMapper, webSocketSenderService, topicHelper);
	}

	@Override
	public boolean canHandleMessage(String fullTopic) {
		return topicHelper.isZigbee2mqttDeviceTopic(fullTopic);
	}

	@Override
	public void handleMessage(String fullTopic, long timestamp, String message) {

		LOGGER.info("Begin Zigbee2mqttMessageListenerHandler ...");
		
		var device = topicHelper.getDevice(fullTopic);

		
		LOGGER.info("fullTopic [{}], timestamp [{}], message [{}]", fullTopic, timestamp, message);
		try {
			message = addTimeStampToMessage(timestamp, message);

			// state message
			webSocketSenderService.publishZigbee2MqttState(fullTopic, message);
			
			applicationContext.setCommandResult(fullTopic, message);
		} catch (JsonProcessingException e) {
			throw new MessagingException("Failed to add message to cache.", e);
		}

		// set and publish connection state
		var stateMessage = new ConnectionStateMessage(ConnectionStateEnum.ONLINE, System.currentTimeMillis());
		applicationContext.setConnectionState(device, stateMessage);
		webSocketSenderService.publishConnectionState(device);
	
		
		LOGGER.info("End Zigbee2mqttMessageListenerHandler ...");
	}
}
