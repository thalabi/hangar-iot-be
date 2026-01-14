package com.kerneldc.hangariot.mqtt.topic;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.kerneldc.hangariot.mqtt.result.tasmota.CommandEnum;
import com.kerneldc.hangariot.mqtt.service.DeviceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * MQTT TopicHelper
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TopicHelper {
	
	public enum TopicSuffixEnum {
		LWT, STATE, POWER, SENSOR, RESULT
	}

	private static final String DEVICE_ARG = "<device>";
	
	private static final String COMMAND_TOPIC_TEMPLATE = "cmnd/<device>/<command>";
	
	// received from MQTT and published on WebSocket
	private static final String LAST_WILL_AND_TESTAMENT_TOPIC_TEMPLATE = "tele/<device>/" + TopicSuffixEnum.LWT;
	private static final String CONNECTION_STATE_TOPIC_TEMPLATE = "tele/<device>/" + TopicSuffixEnum.STATE;
	// received from MQTT and published on WebSocket
	private static final String POWER_TOPIC_TEMPLATE = "stat/<device>/" + TopicSuffixEnum.POWER;
	// received from MQTT and published on WebSocket
	private static final String SENSOR_TOPIC_TEMPLATE = "tele/<device>/" + TopicSuffixEnum.SENSOR;
	// received from MQTT
	private static final String RESULT_TOPIC_TEMPLATE = "stat/<device>/" + TopicSuffixEnum.RESULT;
	
	// ZIGBEE2MQTT
	private static final String ZIGBEE2MQTT_STATE_TOPIC_TEMPLATE = "zigbee2mqtt/<device>";
	
	private final DeviceService deviceService;
	

	public String getCommandTopic(CommandEnum commandEnum, String deviceName) {
		return COMMAND_TOPIC_TEMPLATE.replace(DEVICE_ARG, deviceName)
				.replace("<command>", commandEnum.getCommand());
	}

	public String getStateTopic(String deviceName) {
		return CONNECTION_STATE_TOPIC_TEMPLATE.replace(DEVICE_ARG, deviceName);
	}


	public List<String> getTopicsToSubscribeTo() {
		var topicList = new ArrayList<String>();
		
		for (var device : deviceService.getDeviceList()) {
			switch (device.getBridge()) {
			case TASMOTA -> {
				topicList.add(RESULT_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName()));
				topicList.add(POWER_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName()));
				topicList.add(LAST_WILL_AND_TESTAMENT_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName()));
				if (Boolean.TRUE.equals(device.getTelemetry())) {
					topicList.add(SENSOR_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName()));
				}
			}
			case ZIGBEE2MQTT -> {
				// TODO
				LOGGER.info("Device [{}] bridge is [{}]", device.getName(), device.getBridge());
				topicList.add(ZIGBEE2MQTT_STATE_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName()));
			}
			}
		}
		LOGGER.info("Subscribing to following MQTT topics [{}]", String.join(", ", topicList));
		return topicList;
	}

	public String getDeviceName(String topic) {
		var p = Pattern.compile("^.*/(.*)/.*$");
		var m = p.matcher(topic);
		if (m.matches() && StringUtils.isNotEmpty(m.group(1))) {
				return m.group(1);
		} else {
			return StringUtils.EMPTY;
		}
	}
	
	public String transformLwtToState(String lwtTopic) {
		return lwtTopic.replace("/LWT", "/STATE");
	}
}
