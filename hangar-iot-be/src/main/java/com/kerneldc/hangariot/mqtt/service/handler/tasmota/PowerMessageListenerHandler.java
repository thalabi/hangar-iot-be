package com.kerneldc.hangariot.mqtt.service.handler;

import java.util.Date;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.hangariot.mqtt.message.PowerMessage;
import com.kerneldc.hangariot.mqtt.service.ApplicationCache;
import com.kerneldc.hangariot.mqtt.topic.TopicHelper;
import com.kerneldc.hangariot.mqtt.topic.TopicHelper.TopicSuffixEnum;

@Service
public class PowerMessageListenerHandler extends AbstractMessageListenerHandler {

	public PowerMessageListenerHandler(ApplicationCache applicationCache, ObjectMapper objectMapper,
			SimpMessagingTemplate webSocket, TopicHelper topicHelper) {
		super(applicationCache, objectMapper, webSocket, topicHelper);
	}

	@Override
	public boolean canHandleMessage(String fullTopic) {
		return getTopicSuffix(fullTopic).equals(TopicSuffixEnum.POWER);
	}

	@Override
	public void handleMessage(String fullTopic, long timestamp, String message) {
		
		var powerMessage = new PowerMessage(message, new Date().getTime());

		publishMessageToWebSocket(fullTopic, powerMessage);
	}

}
