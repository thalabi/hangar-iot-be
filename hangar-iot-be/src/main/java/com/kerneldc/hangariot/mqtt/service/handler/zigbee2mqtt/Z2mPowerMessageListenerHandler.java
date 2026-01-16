package com.kerneldc.hangariot.mqtt.service.handler.zigbee2mqtt;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.hangariot.mqtt.service.ApplicationContext;
import com.kerneldc.hangariot.mqtt.service.WebSocketSenderService;
import com.kerneldc.hangariot.mqtt.service.handler.AbstractMessageListenerHandler;
import com.kerneldc.hangariot.mqtt.topic.TopicHelper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class Z2mPowerMessageListenerHandler extends AbstractMessageListenerHandler {

	public Z2mPowerMessageListenerHandler(ApplicationContext applicationContext, ObjectMapper objectMapper,
			WebSocketSenderService webSocketSenderService, TopicHelper topicHelper) {
		super(applicationContext, objectMapper, webSocketSenderService, topicHelper);
	}

	@Override
	public boolean canHandleMessage(String fullTopic) {
		return topicHelper.isZigbee2mqttTopic(fullTopic);
	}

	@Override
	public void handleMessage(String fullTopic, long timestamp, String message) {

		var notDuplicate = applicationContext.setTopicMessage(fullTopic, message);
		if (notDuplicate) {
			LOGGER.info("fullTopic [{}], timestamp [{}], message [{}]", fullTopic, timestamp, message);
		} else {
			LOGGER.info("fullTopic [{}], timestamp [{}], message [{}] *** duplicate and ignored***", fullTopic, timestamp, message);
		}
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
