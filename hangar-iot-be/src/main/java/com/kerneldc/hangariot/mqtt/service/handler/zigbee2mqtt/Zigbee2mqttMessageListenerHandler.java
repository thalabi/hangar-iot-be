package com.kerneldc.hangariot.mqtt.service.handler.zigbee2mqtt;

import java.util.Date;

import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.hangariot.controller.Device;
import com.kerneldc.hangariot.mqtt.message.ConnectionStateEnum;
import com.kerneldc.hangariot.mqtt.message.ConnectionStateMessage;
import com.kerneldc.hangariot.mqtt.message.PowerMessage;
import com.kerneldc.hangariot.mqtt.result.tasmota.CommandEnum;
import com.kerneldc.hangariot.mqtt.result.zigbee2mqtt.StateResult;
import com.kerneldc.hangariot.mqtt.service.ApplicationContext;
import com.kerneldc.hangariot.mqtt.service.WebSocketSenderService;
import com.kerneldc.hangariot.mqtt.service.handler.AbstractMessageListenerHandler;
import com.kerneldc.hangariot.mqtt.topic.TopicHelper;

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
		return topicHelper.isZigbee2mqttTopic(fullTopic);
	}

	@Override
	public void handleMessage(String fullTopic, long timestamp, String message) {

		var device = topicHelper.getDevice(fullTopic);

		var isDuplicate = applicationContext.setTopicMessage(fullTopic, message);
		if (isDuplicate) {

			LOGGER.info("fullTopic [{}], timestamp [{}], message [{}] *** duplicate. only setting new timestamp in cache ***", fullTopic, timestamp, message);
			var stateResult = (StateResult)applicationContext.getCommandResult(device, CommandEnum.ZIGBEE2MQTT_STATE);
			stateResult.setTimestamp(new Date().getTime());
//
//			// publish connection state and power state
//			webSocketSenderService.publishConnectionState(device);
//			var powerMessage = new PowerMessage(stateResult.getState().toLowerCase(), new Date().getTime());
//			webSocketSenderService.publishPowerState(fullTopic, powerMessage);

			return;
		}
		
		LOGGER.info("fullTopic [{}], timestamp [{}], message [{}]", fullTopic, timestamp, message);
		StateResult stateResult;
		try {
			message = addTimeStampToMessage(timestamp, message);
			stateResult = (StateResult)applicationContext.setCommandResult(fullTopic, message);
			LOGGER.info("trace 1. stateResult [{}]", stateResult);
		} catch (JsonProcessingException e) {
			throw new MessagingException("Failed to add message to cache.", e);
		}

		// connection state
		var stateMessage = new ConnectionStateMessage(ConnectionStateEnum.ONLINE, new Date().getTime());
		applicationContext.setConnectionState(device, stateMessage);
		webSocketSenderService.publishConnectionState(device);

		// power state
		var powerMessage = new PowerMessage(stateResult.getState().toLowerCase(), new Date().getTime());
		webSocketSenderService.publishPowerState(fullTopic, powerMessage);
	}
	
	private void publishWebSocketStates(Device device, StateResult stateResult, String fullTopic) {
		// connection state
		var stateMessage = new ConnectionStateMessage(ConnectionStateEnum.ONLINE, new Date().getTime());
		applicationContext.setConnectionState(device, stateMessage);
		webSocketSenderService.publishConnectionState(device);

		// power state
		var powerMessage = new PowerMessage(stateResult.getState().toLowerCase(), new Date().getTime());
		webSocketSenderService.publishPowerState(fullTopic, powerMessage);
		
	}

}
