package com.kerneldc.iot.mqtt.messagehandler.zigbee2mqtt;

import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.iot.mqtt.messagehandler.AbstractMessageListenerHandler;
import com.kerneldc.iot.mqtt.service.ApplicationContext;
import com.kerneldc.iot.mqtt.service.DeviceAttributeLogService;
import com.kerneldc.iot.mqtt.topic.TopicHelper;
import com.kerneldc.iot.websocket.ConnectionStateEnum;
import com.kerneldc.iot.websocket.message.ConnectionStateMessage;
import com.kerneldc.iot.websocket.service.WebSocketSenderService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class Zigbee2mqttMessageListenerHandler extends AbstractMessageListenerHandler {
	
	private final DeviceAttributeLogService deviceAttributeLogService;

	public Zigbee2mqttMessageListenerHandler(ApplicationContext applicationContext, ObjectMapper objectMapper,
			WebSocketSenderService webSocketSenderService, TopicHelper topicHelper, DeviceAttributeLogService deviceAttributeLogService) {
		super(applicationContext, objectMapper, webSocketSenderService, topicHelper);
		this.deviceAttributeLogService = deviceAttributeLogService;
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
			
			// log attribute changes
			var oldState = applicationContext.getZigbee2MqttStateResult(device);
			var newState = applicationContext.setCommandResult(fullTopic, message);
			deviceAttributeLogService.logDiff(device, timestamp, oldState, newState);
			
		} catch (JsonProcessingException e) {
			throw new MessagingException("Failed to add message to cache.", e);
		}

		// set and publish connection state
		var stateMessage = new ConnectionStateMessage(ConnectionStateEnum.ONLINE, System.currentTimeMillis());

		
		applicationContext.setConnectionState(device, stateMessage);
		webSocketSenderService.publishConnectionState(device);
	
		
		LOGGER.info("End Zigbee2mqttMessageListenerHandler ...");
	}

//	private void logDiff(Device device, long timestamp, AbstractBaseResult oldState, AbstractBaseResult newState) {
//		if (oldState == null) {
//			return;
//		}
//		
//		var logDiff = StringUtils.EMPTY;
//		try {
//			LOGGER.info("oldState [{}]", oldState);
//			LOGGER.info("newState [{}]", newState);
//			logDiff = deviceAttributeLogService.diff2(oldState, newState, List.of("state"));
//		} catch (JsonProcessingException e) {
//			throw new MessagingException(NestedExceptionUtils.getMostSpecificCause(e).getMessage());
//		}
//		LOGGER.info("logDiff [{}]", StringUtils.isEmpty(logDiff) ? "no diff" : logDiff);
//	}
}
