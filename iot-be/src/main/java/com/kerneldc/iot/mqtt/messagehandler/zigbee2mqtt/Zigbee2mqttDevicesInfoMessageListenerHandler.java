package com.kerneldc.iot.mqtt.messagehandler.zigbee2mqtt;

import java.util.List;

import org.springframework.core.NestedExceptionUtils;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.iot.domain.mqttdeviceinfo.MqttDeviceInfo;
import com.kerneldc.iot.mqtt.messagehandler.AbstractMessageListenerHandler;
import com.kerneldc.iot.mqtt.result.zigbee2mqtt.MqttDeviceDetails;
import com.kerneldc.iot.mqtt.service.ApplicationContext;
import com.kerneldc.iot.mqtt.service.MqttDeviceInfoService;
import com.kerneldc.iot.mqtt.service.WebSocketSenderService;
import com.kerneldc.iot.mqtt.topic.TopicHelper;
import com.kerneldc.iot.util.TimeUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class Zigbee2mqttDevicesInfoMessageListenerHandler extends AbstractMessageListenerHandler {

	private final MqttDeviceInfoService mqttDeviceInfoService;
	
	public Zigbee2mqttDevicesInfoMessageListenerHandler(ApplicationContext applicationContext, ObjectMapper objectMapper,
			WebSocketSenderService webSocketSenderService, TopicHelper topicHelper, MqttDeviceInfoService mqttDeviceInfoService) {
		super(applicationContext, objectMapper, webSocketSenderService, topicHelper);
		
		this.mqttDeviceInfoService = mqttDeviceInfoService;
	}

	@Override
	public boolean canHandleMessage(String fullTopic) {
		return topicHelper.isDevicesInfoTopic(fullTopic);
	}

	@Override
	public void handleMessage(String fullTopic, long timestamp, String message) {

		LOGGER.info("Begin Zigbee2mqttDevicesInfoMessageListenerHandler ...");
		
		List<MqttDeviceDetails> deviceDetailsList;
		try {
			deviceDetailsList = objectMapper.readValue(message, new TypeReference<List<MqttDeviceDetails>>() {});
		} catch (JsonProcessingException e) {
			throw new MessagingException(String.format("Error serializing devices info message:\n%s", message), NestedExceptionUtils.getMostSpecificCause(e));
		}
		
		LOGGER.info("[{}] device info read from ZIGBEE2MQTT_DEVICES_INFO_TOPIC.", deviceDetailsList.size());
		var i = 0;
		for (var deviceDetails: deviceDetailsList) {
			LOGGER.info("device info [{}], friendlyName [{}] ieeeAddress [{}]", ++i, deviceDetails.getFriendlyName(), deviceDetails.getIeeeAddress());
			var mqttDeviceInfo = new MqttDeviceInfo();
			mqttDeviceInfo.setIeeeAddress(deviceDetails.getIeeeAddress());
			mqttDeviceInfo.setDeviceDetails(deviceDetails);
			mqttDeviceInfo.setTimestamp(TimeUtils.epochMilliToOffsetDateTime(timestamp));
			mqttDeviceInfoService.saveOrUpdate(mqttDeviceInfo);
		}
		
		LOGGER.info("End Zigbee2mqttDevicesInfoMessageListenerHandler ...");
	}
}
