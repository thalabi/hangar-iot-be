package com.kerneldc.iot.mqtt.messagehandler.tasmota;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.iot.mqtt.messagehandler.AbstractMessageListenerHandler;
import com.kerneldc.iot.mqtt.service.ApplicationContext;
import com.kerneldc.iot.mqtt.topic.TopicHelper;
import com.kerneldc.iot.mqtt.topic.TopicHelper.MqttTopicSuffixEnum;
import com.kerneldc.iot.websocket.ConnectionStateEnum;
import com.kerneldc.iot.websocket.message.ConnectionStateMessage;
import com.kerneldc.iot.websocket.service.WebSocketSenderService;


@Service
public class LwtMessageListenerHandler extends AbstractMessageListenerHandler {

	public LwtMessageListenerHandler(ApplicationContext applicationContext, ObjectMapper objectMapper,
			WebSocketSenderService webSocketSenderService, TopicHelper topicHelper) {
		super(applicationContext, objectMapper, webSocketSenderService, topicHelper);
	}

	@Override
	public boolean canHandleMessage(String fullTopic) {
		return topicHelper.isTasmotaTopic(fullTopic) && topicHelper.getTopicSuffix(fullTopic).equals(MqttTopicSuffixEnum.LWT);
	}

	/**
	 * Convert LWT message to STATE message
	 */
	@Override
	public void handleMessage(String fullTopic, long timestamp, String message) {
		
		var stateMessage = new ConnectionStateMessage(ConnectionStateEnum.valueOf(message.toUpperCase()), System.currentTimeMillis());

		applicationContext.setConnectionState(topicHelper.getDeviceFromTopic(fullTopic), stateMessage);

		webSocketSenderService.publishMessageToWebSocket(topicHelper.transformLwtToState(fullTopic), stateMessage);
	}

}
