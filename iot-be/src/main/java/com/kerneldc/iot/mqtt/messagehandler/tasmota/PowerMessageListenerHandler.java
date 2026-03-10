package com.kerneldc.iot.mqtt.messagehandler.tasmota;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.iot.mqtt.messagehandler.AbstractMessageListenerHandler;
import com.kerneldc.iot.mqtt.service.ApplicationContext;
import com.kerneldc.iot.mqtt.service.WebSocketSenderService;
import com.kerneldc.iot.mqtt.topic.TopicHelper;
import com.kerneldc.iot.mqtt.topic.TopicHelper.MqttTopicSuffixEnum;
import com.kerneldc.iot.websocket.message.PowerMessage;

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
		var powerMessage = new PowerMessage(message.toLowerCase(), System.currentTimeMillis());

		webSocketSenderService.publishPowerState(fullTopic, powerMessage);
	}

}
