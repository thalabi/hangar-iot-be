package com.kerneldc.hangariot.mqtt.service.handler.zigbee2mqtt;

import java.util.Date;

import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.hangariot.mqtt.message.ConnectionStateEnum;
import com.kerneldc.hangariot.mqtt.message.ConnectionStateMessage;
import com.kerneldc.hangariot.mqtt.result.tasmota.CommandEnum;
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
			LOGGER.info("fullTopic [{}], timestamp [{}], message [{}] *** duplicate and ignored ***", fullTopic, timestamp, message);
			var stateResult = applicationContext.getCommandResult(device, CommandEnum.ZIGBEE2MQTT_STATE);
			LOGGER.info("stateResult [{}]", stateResult);
			stateResult.setTimestamp(new Date().getTime());
			var stateResult2 = applicationContext.getCommandResult(device, CommandEnum.ZIGBEE2MQTT_STATE);
			LOGGER.info("stateResult2 [{}]", stateResult2);
			return;
		}
		LOGGER.info("fullTopic [{}], timestamp [{}], message [{}]", fullTopic, timestamp, message);
		try {
			message = addTimeStampToMessage(timestamp, message);
			applicationContext.setCommandResult(fullTopic, message);
		} catch (JsonProcessingException e) {
			throw new MessagingException("Failed to add message to cache.", e);
		}

		var stateMessage = new ConnectionStateMessage(ConnectionStateEnum.ONLINE, new Date().getTime());
		applicationContext.setConnectionState(device, stateMessage);

		webSocketSenderService.publishConnectionState(device);

/*
		try {
			message = addTimeStampToMessage(timestamp, message);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new MessagingException("Error adding timestamp field to json string", NestedExceptionUtils.getMostSpecificCause(e));
		}		

		try {
			applicationContext.setCommandResult(fullTopic, message);
		} catch (JsonProcessingException e) {
			throw new MessagingException("Failed to add message to cache.", e);
		}
*/
	}

}
