package com.kerneldc.hangariot.mqtt.messagehandler.zigbee2mqtt;

import java.util.List;

import org.springframework.core.NestedExceptionUtils;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.hangariot.domain.devicesinfo.DevicesInfo;
import com.kerneldc.hangariot.mqtt.messagehandler.AbstractMessageListenerHandler;
import com.kerneldc.hangariot.mqtt.result.zigbee2mqtt.MqttDeviceDetails;
import com.kerneldc.hangariot.mqtt.service.ApplicationContext;
import com.kerneldc.hangariot.mqtt.service.WebSocketSenderService;
import com.kerneldc.hangariot.mqtt.topic.TopicHelper;
import com.kerneldc.hangariot.repository.DevicesInfoRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class Zigbee2mqttDevicesInfoMessageListenerHandler extends AbstractMessageListenerHandler {

	private final DevicesInfoRepository devicesInfoRepository;
	
	public Zigbee2mqttDevicesInfoMessageListenerHandler(ApplicationContext applicationContext, ObjectMapper objectMapper,
			WebSocketSenderService webSocketSenderService, TopicHelper topicHelper, DevicesInfoRepository devicesInfoRepository) {
		super(applicationContext, objectMapper, webSocketSenderService, topicHelper);
		
		this.devicesInfoRepository = devicesInfoRepository;
	}

	@Override
	public boolean canHandleMessage(String fullTopic) {
		return topicHelper.isZigbee2mqttDevicesInfoTopic(fullTopic);
	}

	@Override
	public void handleMessage(String fullTopic, long timestamp, String message) {

		LOGGER.info("Begin Zigbee2mqttDevicesInfoMessageListenerHandler ...");
		
		LOGGER.info("Updating DevicesInfo entity");
		var devicesInfo = devicesInfoRepository.findByKey(DevicesInfo.KEY);
		devicesInfo.setDevicesInfo(message);
		devicesInfoRepository.save(devicesInfo);
		List<MqttDeviceDetails> deviceDetailsList;
		try {
			deviceDetailsList = objectMapper.readValue(message, new TypeReference<List<MqttDeviceDetails>>() {});
		} catch (JsonProcessingException e) {
			throw new MessagingException(String.format("Error serializing devices info message:\n%s", message), NestedExceptionUtils.getMostSpecificCause(e));
		}		
		LOGGER.info("device info read: [{}]", deviceDetailsList.size());
		var i = 0;
		for (var deviceDetails: deviceDetailsList) {
			LOGGER.info("device [{}], friendlyName [{}] ieeeAddress [{}]", ++i, deviceDetails.getFriendlyName(), deviceDetails.getIeeeAddress());
		}
		
		LOGGER.info("End Zigbee2mqttDevicesInfoMessageListenerHandler ...");
	}
}
