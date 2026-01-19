package com.kerneldc.hangariot.mqtt.topic;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.kerneldc.hangariot.controller.Device;
import com.kerneldc.hangariot.controller.Device.BridgeEnum;
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
	
	public enum MqttTopicSuffixEnum {
		LWT, STATE, POWER, SENSOR, RESULT
	}

	private static final String DEVICE_ARG = "<device>";
	
	private static final String COMMAND_TOPIC_TEMPLATE = "cmnd/<device>/<command>";
	
	// received from MQTT and published on WebSocket
	private static final String LAST_WILL_AND_TESTAMENT_TOPIC_TEMPLATE = "tele/<device>/" + MqttTopicSuffixEnum.LWT;
	
//	private static final String WS_CONNECTION_STATE_TOPIC_TEMPLATE = "tele/<device>/" + MqttTopicSuffixEnum.STATE;
	private static final String WS_CONNECTION_STATE_TOPIC_TEMPLATE = "<device>/state";
	// received from MQTT and published on WebSocket
	private static final String POWER_TOPIC_TEMPLATE = "stat/<device>/" + MqttTopicSuffixEnum.POWER;
	// received from MQTT and published on WebSocket
	private static final String SENSOR_TOPIC_TEMPLATE = "tele/<device>/" + MqttTopicSuffixEnum.SENSOR;
	// received from MQTT
	private static final String RESULT_TOPIC_TEMPLATE = "stat/<device>/" + MqttTopicSuffixEnum.RESULT;
	
	// ZIGBEE2MQTT
	private static final String MQTT_ZIGBEE2MQTT_STATE_TOPIC_TEMPLATE = "zigbee2mqtt/<device>";
	private static final String MQTT_ZIGBEE2MQTT_GET_TOPIC_TEMPLATE = "zigbee2mqtt/<device>/get";
	private static final String MQTT_ZIGBEE2MQTT_SET_TOPIC_TEMPLATE = "zigbee2mqtt/<device>/set";
	
	private final DeviceService deviceService;
	

//	public String getCommandTopic(CommandEnum commandEnum, String deviceName) {
//		return COMMAND_TOPIC_TEMPLATE.replace(DEVICE_ARG, deviceName)
//				.replace("<command>", commandEnum.getCommand());
//	}
	public String getCommandTopic(CommandEnum commandEnum, Device device) {
		var bridge = device.getBridge();
		if (bridge == BridgeEnum.ZIGBEE2MQTT) {
			if (commandEnum == CommandEnum.ZIGBEE2MQTT_STATE) { // TODO how to determine if this a get or a set state operation?
				return MQTT_ZIGBEE2MQTT_GET_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName());
			} else {
				return MQTT_ZIGBEE2MQTT_SET_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName());
			}
		}
		if (bridge == BridgeEnum.TASMOTA) {
			return COMMAND_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName()).replace("<command>", commandEnum.getCommand());
		}
		throw new IllegalStateException();
	}

	public String getWsStateTopic(Device device) {
		return switch (device.getBridge()) {
		case TASMOTA ->
			WS_CONNECTION_STATE_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName());
		case ZIGBEE2MQTT -> 
			WS_CONNECTION_STATE_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName());
		};
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
				topicList.add(MQTT_ZIGBEE2MQTT_STATE_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName()));
				
			}
			}
		}
		LOGGER.info("Subscribing to following MQTT topics [{}]", String.join(", ", topicList));
		return topicList;
	}

	// The first pattern is for TASMOTA topics and the second is for ZIGBEE2MQTT topics
	private static final Pattern TOPIC_PATTERN = Pattern.compile("(.+/(.+)/.+)|(zigbee2mqtt/(.+))");

	public Device getDevice(String topic) {
		var matcher = TOPIC_PATTERN.matcher(topic);

		if (! /* not */ matcher.matches()) {
			throw new IllegalArgumentException(String.format("Topic [%s] does not match any known patterns", topic));
		}

		// If the first (TASMOTA) pattern matched, group(2) will have the value
		// If the second (ZIGBEE2MQTT) pattern matched, group(4) will have the value
		var deviceName = (matcher.group(2) != null) ? matcher.group(2) : matcher.group(4);
		if (StringUtils.isEmpty(deviceName)) {
			throw new IllegalStateException(String.format("Could not extract device name from topic [%s]", topic));
		}
		return deviceService.getDevice(deviceName);
	}
	
	public String transformLwtToState(String lwtTopic) {
		return lwtTopic.replace("/LWT", "/STATE");
	}
	
	public boolean isTasmotaTopic(String topic) {
		return topic.startsWith("stat/") || topic.startsWith("tele/");
	}
	public boolean isZigbee2mqttTopic(String topic) {
		return topic.startsWith("zigbee2mqtt/");
	}
	
	public MqttTopicSuffixEnum getTopicSuffix(String topic) {
		var pattern = Pattern.compile("^(.+)/(.+)/(.+)$");
		var matcher = pattern.matcher(topic);
		if (! /* not */ matcher.matches()) {
			throw new IllegalArgumentException(String.format("Could not get suffix from %s", topic));
		}
		return MqttTopicSuffixEnum.valueOf(matcher.group(3));
		
	}
	public String getZ2mConnectionStateTopic(Device device) {
		return MQTT_ZIGBEE2MQTT_GET_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName());
	}

}
