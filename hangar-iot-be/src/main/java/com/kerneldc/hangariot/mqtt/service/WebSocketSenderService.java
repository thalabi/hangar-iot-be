package com.kerneldc.hangariot.mqtt.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.kerneldc.hangariot.mqtt.topic.TopicHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketSenderService {

	private final SimpMessagingTemplate webSocket;
	private final ApplicationCache applicationCache;
	private final TopicHelper topicHelper;
	private final DeviceService deviceService;

	@Value("${websocket.topics.prefix:/topic}")
	private String websocketTopicsPrefix;

	public void triggerPublishConnectionState(String deviceName) {
		LOGGER.info("Publishing ConnectionStateMessage message [{}] of device [{}]",
				applicationCache.getConnectionState(deviceName), deviceName);
		switch (deviceService.getDevice(deviceName).getBridge()) {
		case TASMOTA -> {
			var webSocketTopic = websocketTopicsPrefix + "/state-and-telemetry/"
					+ topicHelper.getStateTopic(deviceName);
			webSocket.convertAndSend(webSocketTopic, applicationCache.getConnectionState(deviceName));
		}
		case ZIGBEE2MQTT -> {

		}
		}
	}

}
