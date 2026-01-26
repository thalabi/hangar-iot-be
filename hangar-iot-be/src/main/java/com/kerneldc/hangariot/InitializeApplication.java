package com.kerneldc.hangariot;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.stereotype.Component;

import com.kerneldc.hangariot.domain.enums.BridgeEnum;
import com.kerneldc.hangariot.mqtt.service.ApplicationContext;
import com.kerneldc.hangariot.mqtt.service.DeviceService;
import com.kerneldc.hangariot.mqtt.service.MqttSenderService;
import com.kerneldc.hangariot.mqtt.topic.TopicHelper;
import com.kerneldc.hangariot.websocket.ConnectionStateEnum;
import com.kerneldc.hangariot.websocket.message.ConnectionStateMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitializeApplication {

	private final MqttPahoMessageDrivenChannelAdapter mqtt;
	private final DeviceService deviceService;
	private final ApplicationContext applicationContext;
	private final TopicHelper topicHelper;
	private final MqttSenderService mqttSenderService;
	
	@EventListener(ApplicationReadyEvent.class)
    public void init() {
		
		startMqtt();
		connectionStateOfZ2mDevices();
	}
	
	private void startMqtt() {
		var topicList = topicHelper.getTopicsToSubscribeTo();
		LOGGER.info("Subscribing to following MQTT topics [{}]", String.join(", ", topicList));
		mqtt.addTopic(topicList.toArray(new String[0]));
		LOGGER.info("Starting MQTT.");
		mqtt.start();
	}
	
	private void connectionStateOfZ2mDevices() {
		
		LOGGER.info("Setting the connection state of Zigbee2Mqtt devices:");
		var i = 0;
		for (var device: deviceService.getDeviceList()) {
			if (device.getBridge() == BridgeEnum.ZIGBEE2MQTT) {
				LOGGER.info("{} - device [{}]", String.format("%2d", ++i), device.getName());
				if (BooleanUtils.isTrue(device.getPassive())) {
					applicationContext.setConnectionState(device, new ConnectionStateMessage(ConnectionStateEnum.PASSIVE, System.currentTimeMillis()));
				} else {
					mqttSenderService.triggerPublishConnectionState(device);
				}
			}
		}
		if (i == 0) {
			LOGGER.warn("  No Zigbee2Mqtt devices found.");
		}
	}

}
