package com.kerneldc.hangariot.mqtt.service.handler.tasmota;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.hangariot.mqtt.message.ConnectionStateEnum;
import com.kerneldc.hangariot.mqtt.message.ConnectionStateMessage;
import com.kerneldc.hangariot.mqtt.service.ApplicationContext;
import com.kerneldc.hangariot.mqtt.service.WebSocketSenderService;
import com.kerneldc.hangariot.mqtt.service.handler.AbstractMessageListenerHandler;
import com.kerneldc.hangariot.mqtt.topic.TopicHelper;
import com.kerneldc.hangariot.mqtt.topic.TopicHelper.TopicSuffixEnum;


@Service
public class LwtMessageListenerHandler extends AbstractMessageListenerHandler {

	public LwtMessageListenerHandler(ApplicationContext applicationContext, ObjectMapper objectMapper,
			WebSocketSenderService webSocketSenderService, TopicHelper topicHelper) {
		super(applicationContext, objectMapper, webSocketSenderService, topicHelper);
	}

	@Override
	public boolean canHandleMessage(String fullTopic) {
		return topicHelper.isTasmotaTopic(fullTopic) && topicHelper.getTopicSuffix(fullTopic).equals(TopicSuffixEnum.LWT);
	}

	/**
	 * Convert LWT message to STATE message
	 */
	@Override
	public void handleMessage(String fullTopic, long timestamp, String message) {
		
		var stateMessage = new ConnectionStateMessage(ConnectionStateEnum.valueOf(message.toUpperCase()), new Date().getTime());

		applicationContext.setConnectionState(topicHelper.getDevice(fullTopic), stateMessage);

		webSocketSenderService.publishMessageToWebSocket(topicHelper.transformLwtToState(fullTopic), stateMessage);
	}

}
