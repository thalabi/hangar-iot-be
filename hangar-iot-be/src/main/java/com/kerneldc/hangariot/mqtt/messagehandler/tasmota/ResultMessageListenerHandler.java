package com.kerneldc.hangariot.mqtt.messagehandler.tasmota;

import org.springframework.core.NestedExceptionUtils;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.hangariot.mqtt.messagehandler.AbstractMessageListenerHandler;
import com.kerneldc.hangariot.mqtt.service.ApplicationContext;
import com.kerneldc.hangariot.mqtt.service.WebSocketSenderService;
import com.kerneldc.hangariot.mqtt.topic.TopicHelper;
import com.kerneldc.hangariot.mqtt.topic.TopicHelper.MqttTopicSuffixEnum;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ResultMessageListenerHandler extends AbstractMessageListenerHandler {

	public ResultMessageListenerHandler(ApplicationContext applicationContext, ObjectMapper objectMapper,
			WebSocketSenderService webSocketSenderService, TopicHelper topicHelper) {
		super(applicationContext, objectMapper, webSocketSenderService, topicHelper);
	}

	@Override
	public boolean canHandleMessage(String fullTopic) {
		return topicHelper.isTasmotaTopic(fullTopic) && topicHelper.getTopicSuffix(fullTopic).equals(MqttTopicSuffixEnum.RESULT);
	}

	@Override
	public void handleMessage(String fullTopic, long timestamp, String message) {
		LOGGER.info("handle()");

		LOGGER.info("fullTopic [{}], timestamp [{}], message [{}]", fullTopic, timestamp, message);
		try {
			message = addTimeStampToMessage(timestamp, message);
		} catch (JsonProcessingException e) {
			throw new MessagingException("Error adding timestamp field to json string", NestedExceptionUtils.getMostSpecificCause(e));
		}		

		try {
			applicationContext.setCommandResult(fullTopic, message);
		} catch (JsonProcessingException e) {
			throw new MessagingException("Failed to add message to cache.", e);
		}

	}

}
