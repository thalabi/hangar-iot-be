package com.kerneldc.iot.mqtt.topic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.kerneldc.iot.domain.device.Device;
import com.kerneldc.iot.domain.enums.BridgeEnum;
import com.kerneldc.iot.mqtt.command.ICommandEnum;
import com.kerneldc.iot.mqtt.command.TasmotaCommandEnum;
import com.kerneldc.iot.mqtt.command.Zigbee2MqttCommandEnum;
import com.kerneldc.iot.mqtt.service.DeviceService;

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
	private static final String COMMAND_ARG = "<command>";
	private static final String ADDRESS_ARG = "<address>";
	private static final String AREA_ARG = "<area>";
	
	private static final String COMMAND_TOPIC_TEMPLATE = "cmnd/<device>/<command>";
	
	// received from MQTT and published on WebSocket
	private static final String LAST_WILL_AND_TESTAMENT_TOPIC_TEMPLATE = "tele/<device>/" + MqttTopicSuffixEnum.LWT;
	
	private static final String WS_CONNECTION_STATE_TOPIC_TEMPLATE = "<device>/state";
	private static final String WS_STATE_TOPIC_TEMPLATE = "zigbee2mqtt/<device>";
	// received from MQTT and published on WebSocket
	private static final String MQTT_TASMOTA_POWER_TOPIC_TEMPLATE = "stat/<device>/" + MqttTopicSuffixEnum.POWER;
	// received from MQTT and published on WebSocket
	private static final String MQTT_TASMOTA_SENSOR_TOPIC_TEMPLATE = "tele/<device>/" + MqttTopicSuffixEnum.SENSOR;
	// received from MQTT
	private static final String MQTT_TASMOTA_RESULT_TOPIC_TEMPLATE = "stat/<device>/" + MqttTopicSuffixEnum.RESULT;
	
	private static final String MQTT_ZIGBEE_DEVICES_INFO_TOPIC = "zigbee2mqtt/bridge/devices";
	private static final String MQTT_ZIGBEE_TOPIC_TEMPLATE = "zigbee2mqtt/<device>";
	private static final String MQTT_ZIGBEE_STATE_TOPIC_TEMPLATE = "zigbee2mqtt/<device>";
	private static final String ESPRESENSE_STATE_TOPIC_TEMPLATE = "espresense/devices/<address>/<area>";
	
	private final DeviceService deviceService;
	
	public String getCommandTopic(ICommandEnum commandEnum, Device device) {
		switch (commandEnum.handlesBridge()) {
		case ZIGBEE2MQTT: {
			return MQTT_ZIGBEE_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName()) + ((Zigbee2MqttCommandEnum)commandEnum).getSubTopic();
		}
		case TASMOTA: {
			return COMMAND_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName()).replace(COMMAND_ARG, ((TasmotaCommandEnum)commandEnum).getCommand());
		}
		}
		throw new IllegalStateException();
	}

	public String getWsConnectionStateTopic(Device device) {
		return WS_CONNECTION_STATE_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName());
	}
	public String getWsStateTopic(Device device) {
		return WS_STATE_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName());
	}


	public List<String> getTopicsToSubscribeTo() {
		var topicList = new ArrayList<String>();
		
		topicList.add(MQTT_ZIGBEE_DEVICES_INFO_TOPIC);
		
		// Only return topic for devices that are managed ie isManaged is true
		deviceService.getManagedDeviceList().forEach(device -> {
				switch (device.getBridge()) {
				case TASMOTA -> {
					topicList.add(MQTT_TASMOTA_RESULT_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName()));
					topicList.add(MQTT_TASMOTA_POWER_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName()));
					topicList.add(LAST_WILL_AND_TESTAMENT_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName()));
					if (Boolean.TRUE.equals(device.getTelemetry())) {
						topicList.add(MQTT_TASMOTA_SENSOR_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName()));
					}
				}
				case ZIGBEE2MQTT -> {
					topicList.add(MQTT_ZIGBEE_STATE_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName()));
				}
				case ESPRESENSE -> {
					Objects.requireNonNull(device.getArea(), "Device [" + device.getName() + "] area cannot be null");
					topicList.add(ESPRESENSE_STATE_TOPIC_TEMPLATE.replace(ADDRESS_ARG, device.getAddress())
																	.replace(AREA_ARG, device.getArea().getName().toLowerCase()));
				}
				default -> throw new IllegalArgumentException("Unexpected value: " + device.getBridge());
				}
				}
				);
		return topicList;
	}

	// The first pattern is for TASMOTA topics, the second is for ZIGBEE2MQTT topics and the third is for ESPresense topics
	private static final Pattern TOPIC_PATTERN = Pattern.compile("^(?:[^/]*/([^/]+)/[^/]*|zigbee2mqtt/([^/]+)|espresense/[^/]*/([^/]+)/[^/]*)$");


	public Device getDeviceFromTopic(String topic) {
		var matcher = TOPIC_PATTERN.matcher(topic);

		if (! /* not */ matcher.matches()) {
			throw new IllegalArgumentException(String.format("Topic [%s] does not match any known patterns", topic));
		}

		// If the first (TASMOTA) pattern matched, group(1) will have the value
		// If the second (ZIGBEE2MQTT) pattern matched, group(2) will have the value
		// If the second (ESPresense) pattern matched, group(3) will have the value
		String token;
		if (matcher.group(1) != null) {
			token = matcher.group(1);
			return deviceService.getDeviceByName(token);
		}
		if (matcher.group(2) != null) {
			token = matcher.group(2);
			return deviceService.getDeviceByName(token);
		}
		if (matcher.group(3) != null) {
			token = matcher.group(3);
			return deviceService.getDeviceByAddress(token);
		}

		throw new IllegalStateException(String.format("Could not extract device from topic [%s]", topic));
	}
	
	public String transformLwtToState(String lwtTopic) {
		return lwtTopic.replace("/LWT", "/STATE");
	}
	
	public boolean isTasmotaTopic(String topic) {
		return topic.startsWith("stat/") || topic.startsWith("tele/");
	}
	public boolean isZigbee2mqttDeviceTopic(String topic) {
		return topic.startsWith("zigbee2mqtt/") && ! /* not */ topic.equals(MQTT_ZIGBEE_DEVICES_INFO_TOPIC);
	}
	public boolean isEspresenseDeviceTopic(String topic) {
		return topic.startsWith("espresense/");
	}

	public boolean isDevicesInfoTopic(String topic) {
		return StringUtils.equals(topic, MQTT_ZIGBEE_DEVICES_INFO_TOPIC);
	}
	
	public MqttTopicSuffixEnum getTopicSuffix(String topic) {
		var pattern = Pattern.compile("^(.+)/(.+)/(.+)$");
		var matcher = pattern.matcher(topic);
		if (! /* not */ matcher.matches()) {
			throw new IllegalArgumentException(String.format("Could not get suffix from %s", topic));
		}
		return MqttTopicSuffixEnum.valueOf(matcher.group(3));
	}
	

}
