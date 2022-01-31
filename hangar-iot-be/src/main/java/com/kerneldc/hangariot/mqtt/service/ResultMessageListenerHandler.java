package com.kerneldc.hangariot.mqtt.service;

import org.springframework.core.NestedExceptionUtils;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.hangariot.mqtt.topic.TopicHelper;
import com.kerneldc.hangariot.mqtt.topic.TopicHelper.TopicSuffixEnum;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ResultMessageListenerHandler extends AbstractMessageListenerHandlerService {

	public ResultMessageListenerHandler(ApplicationCache applicationCache, ObjectMapper objectMapper,
			SimpMessagingTemplate webSocket, TopicHelper topicHelper) {
		super(applicationCache, objectMapper, webSocket, topicHelper);
	}

	@Override
	public boolean canHandleMessage(String fullTopic) {
		return getTopicSuffix(fullTopic).equals(TopicSuffixEnum.RESULT);
	}

	@Override
	public void handleMessage(String fullTopic, long timestamp, String message) {

		try {
			message = addTimeStampToMessage(timestamp, message);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new MessagingException("Error parsing message string as a JSON object", NestedExceptionUtils.getMostSpecificCause(e));
		}		

		try {
			applicationCache.setCommandResult(fullTopic, message);
		} catch (JsonProcessingException e) {
			throw new MessagingException("Failed to add message to cache.", e);
		}

	}

}