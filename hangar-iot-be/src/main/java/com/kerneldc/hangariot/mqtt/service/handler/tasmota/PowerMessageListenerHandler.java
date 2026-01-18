package com.kerneldc.hangariot.mqtt.service.handler.tasmota;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.hangariot.mqtt.message.PowerMessage;
import com.kerneldc.hangariot.mqtt.service.ApplicationContext;
import com.kerneldc.hangariot.mqtt.service.WebSocketSenderService;
import com.kerneldc.hangariot.mqtt.service.handler.AbstractMessageListenerHandler;
import com.kerneldc.hangariot.mqtt.topic.TopicHelper;
import com.kerneldc.hangariot.mqtt.topic.TopicHelper.MqttTopicSuffixEnum;

@Service
public class PowerMessageListenerHandler extends AbstractMessageListenerHandler {

	public PowerMessageListenerHandler(ApplicationContext applicationContext, ObjectMapper objectMapper,
			WebSocketSenderService webSocketSenderService, TopicHelper topicHelper) {
		super(applicationContext, objectMapper, webSocketSenderService, topicHelper);
	}

	@Override
	public boolean canHandleMessage(String fullTopic) {
		return topicHelper.isTasmotaTopic(fullTopic) && topicHelper.getTopicSuffix(fullTopic).equals(MqttTopicSuffixEnum.POWER);
	}

	@Override
	public void handleMessage(String fullTopic, long timestamp, String message) {
		
		var powerMessage = new PowerMessage(message, new Date().getTime());

		webSocketSenderService.publishMessageToWebSocket(fullTopic, powerMessage);
	}

}
