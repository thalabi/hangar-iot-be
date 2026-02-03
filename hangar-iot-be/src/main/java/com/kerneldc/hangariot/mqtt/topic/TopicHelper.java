package com.kerneldc.hangariot.mqtt.topic;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.kerneldc.hangariot.domain.device.Device;
import com.kerneldc.hangariot.mqtt.command.ICommandEnum;
import com.kerneldc.hangariot.mqtt.command.TasmotaCommandEnum;
import com.kerneldc.hangariot.mqtt.command.Zigbee2MqttCommandEnum;
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
	private static final String COMMAND_ARG = "<command>";
	
	private static final String COMMAND_TOPIC_TEMPLATE = "cmnd/<device>/<command>";
	
	// received from MQTT and published on WebSocket
	private static final String LAST_WILL_AND_TESTAMENT_TOPIC_TEMPLATE = "tele/<device>/" + MqttTopicSuffixEnum.LWT;
	
	private static final String WS_CONNECTION_STATE_TOPIC_TEMPLATE = "<device>/state";
	// received from MQTT and published on WebSocket
	private static final String POWER_TOPIC_TEMPLATE = "stat/<device>/" + MqttTopicSuffixEnum.POWER;
	// received from MQTT and published on WebSocket
	private static final String SENSOR_TOPIC_TEMPLATE = "tele/<device>/" + MqttTopicSuffixEnum.SENSOR;
	// received from MQTT
	private static final String RESULT_TOPIC_TEMPLATE = "stat/<device>/" + MqttTopicSuffixEnum.RESULT;
	
	// ZIGBEE2MQTT
	private static final String MQTT_ZIGBEE2MQTT_DEVICES_INFO_TOPIC = "zigbee2mqtt/bridge/devices";
	private static final String MQTT_ZIGBEE2MQTT_TOPIC_TEMPLATE = "zigbee2mqtt/<device>";
	private static final String MQTT_ZIGBEE2MQTT_STATE_TOPIC_TEMPLATE = "zigbee2mqtt/<device>";
	
	private final DeviceService deviceService;
	
	public String getCommandTopic(ICommandEnum commandEnum, Device device) {
		switch (commandEnum.handlesBridge()) {
		case ZIGBEE2MQTT: {
			return MQTT_ZIGBEE2MQTT_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName()) + ((Zigbee2MqttCommandEnum)commandEnum).getSubTopic();
		}
		case TASMOTA: {
			return COMMAND_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName()).replace(COMMAND_ARG, ((TasmotaCommandEnum)commandEnum).getCommand());
		}
		}
		throw new IllegalStateException();
	}

	public String getWsStateTopic(Device device) {
		return WS_CONNECTION_STATE_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName());
	}


	public List<String> getTopicsToSubscribeTo() {
		var topicList = new ArrayList<String>();
		
		topicList.add(MQTT_ZIGBEE2MQTT_DEVICES_INFO_TOPIC);
		
		// Only return topic for devices that are managed ie isManaged is true
		deviceService.getManagedDeviceList().forEach(device -> {
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
					topicList.add(MQTT_ZIGBEE2MQTT_STATE_TOPIC_TEMPLATE.replace(DEVICE_ARG, device.getName()));
				}
				}
				}
				);
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
	public boolean isZigbee2mqttDeviceTopic(String topic) {
		return topic.startsWith("zigbee2mqtt/") && ! /* not */ topic.equals(MQTT_ZIGBEE2MQTT_DEVICES_INFO_TOPIC);
	}
	public boolean isZigbee2mqttDevicesInfoTopic(String topic) {
		return topic.equals(MQTT_ZIGBEE2MQTT_DEVICES_INFO_TOPIC);
	}
	
	public MqttTopicSuffixEnum getTopicSuffix(String topic) {
		var pattern = Pattern.compile("^(.+)/(.+)/(.+)$");
		var matcher = pattern.matcher(topic);
		if (! /* not */ matcher.matches()) {
			throw new IllegalArgumentException(String.format("Could not get suffix from %s", topic));
		}
		return MqttTopicSuffixEnum.valueOf(matcher.group(3));
	}
	
	public boolean isDevicesInfoTopic(String topic) {
		return StringUtils.equals(topic, MQTT_ZIGBEE2MQTT_DEVICES_INFO_TOPIC);
	}

}
