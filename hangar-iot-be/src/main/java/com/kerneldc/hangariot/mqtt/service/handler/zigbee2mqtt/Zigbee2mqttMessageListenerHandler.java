package com.kerneldc.hangariot.mqtt.service.handler.zigbee2mqtt;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.hangariot.mqtt.command.Zigbee2MqttCommandEnum;
import com.kerneldc.hangariot.mqtt.result.zigbee2mqtt.StateResult;
import com.kerneldc.hangariot.mqtt.service.ApplicationContext;
import com.kerneldc.hangariot.mqtt.service.WebSocketSenderService;
import com.kerneldc.hangariot.mqtt.service.handler.AbstractMessageListenerHandler;
import com.kerneldc.hangariot.mqtt.topic.TopicHelper;
import com.kerneldc.hangariot.websocket.ConnectionStateEnum;
import com.kerneldc.hangariot.websocket.message.ConnectionStateMessage;
import com.kerneldc.hangariot.websocket.message.PowerMessage;

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

		LOGGER.info("Begin Zigbee2mqttMessageListenerHandler ...");
		
		var device = topicHelper.getDevice(fullTopic);

		var isDuplicate = applicationContext.setTopicMessage(fullTopic, message);
		if (isDuplicate) {

			LOGGER.info("fullTopic [{}], timestamp [{}], message [{}] *** duplicate. only setting new timestamp in cache ***", fullTopic, timestamp, message);
			var stateResult = (StateResult)applicationContext.getCommandResult(device, Zigbee2MqttCommandEnum.STATE);
			stateResult.setTimestamp(System.currentTimeMillis());

			// publish connection state
			webSocketSenderService.publishConnectionState(device);
			
			// publish power state
			if (BooleanUtils.isFalse(device.getPassive())) {
				var powerMessage = new PowerMessage(stateResult.getState().toLowerCase(), System.currentTimeMillis());
				webSocketSenderService.publishPowerState(fullTopic, powerMessage);
			}

		} else {
		
			LOGGER.info("fullTopic [{}], timestamp [{}], message [{}]", fullTopic, timestamp, message);
			StateResult stateResult;
			try {
				message = addTimeStampToMessage(timestamp, message);
				stateResult = (StateResult)applicationContext.setCommandResult(fullTopic, message);
			} catch (JsonProcessingException e) {
				throw new MessagingException("Failed to add message to cache.", e);
			}
	
			// set and publish connection state
			var stateMessage = new ConnectionStateMessage(ConnectionStateEnum.ONLINE, System.currentTimeMillis());
			applicationContext.setConnectionState(device, stateMessage);
			webSocketSenderService.publishConnectionState(device);
	
			// power state
			if (BooleanUtils.isFalse(device.getPassive())) {
				var powerMessage = new PowerMessage(stateResult.getState().toLowerCase(), System.currentTimeMillis());
				webSocketSenderService.publishPowerState(fullTopic, powerMessage);
			}
		}
		
		LOGGER.info("End Zigbee2mqttMessageListenerHandler ...");
	}
}
