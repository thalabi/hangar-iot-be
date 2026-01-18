package com.kerneldc.hangariot.mqtt.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kerneldc.hangariot.controller.Device;
import com.kerneldc.hangariot.controller.Device.BridgeEnum;
import com.kerneldc.hangariot.mqtt.message.ConnectionStateEnum;
import com.kerneldc.hangariot.mqtt.message.ConnectionStateMessage;
import com.kerneldc.hangariot.mqtt.result.AbstractBaseResult;
import com.kerneldc.hangariot.mqtt.result.tasmota.CommandEnum;
import com.kerneldc.hangariot.mqtt.topic.TopicHelper;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationContext {
	
	private final ObjectMapper objectMapper;
	private final TopicHelper topicHelper;
	private Map<DeviceAndCommandEnum, AbstractBaseResult> resultTopicCache = new ConcurrentHashMap<>();
	private Map<Device, ConnectionStateMessage> deviceConnectionStateCache = new ConcurrentHashMap<>();
	
	private record DeviceAndCommandEnum(Device device, CommandEnum commandEnum) {}
	
	// Used to detect duplicates
	private Map<String, String> topicMessageCache = new ConcurrentHashMap<>();

	/**
	 * @param topic
	 * @param message
	 * @return the stateResult as a json
	 * @throws JsonProcessingException
	 */
	public AbstractBaseResult setCommandResult(String topic, String message) throws JsonProcessingException {
		//var deviceName = extractDeviceName(topic);
//		var device = extractDevice(topic);
		var device = topicHelper.getDevice(topic);
		CommandEnum commandEnum; 
		if (device.getBridge() == BridgeEnum.ZIGBEE2MQTT) {
			commandEnum = CommandEnum.ZIGBEE2MQTT_STATE;
		} else {
			commandEnum = getTasmotaCommandEnum(message);
		}
	    if (commandEnum == null) {
	    	throw new IllegalArgumentException(String.format("Message type of [%s] is not supported. Can't add it to cache", message));
	    }
		var result = objectMapper.readValue(message, commandEnum.getResultType());
		resultTopicCache.put(new DeviceAndCommandEnum(device, commandEnum), result);
		return result;
	}

	public AbstractBaseResult getCommandResult(Device device, CommandEnum commandEnum) {
		return resultTopicCache.get(new DeviceAndCommandEnum(device, commandEnum));
	}

	// Tasmota message
	private CommandEnum getTasmotaCommandEnum(String message) throws JsonProcessingException {
		
		var jsonObject = objectMapper.readValue(message, ObjectNode.class);
		try {
			return CommandEnum.valueOf(jsonObject.fieldNames().next().toUpperCase());
		} catch (IllegalArgumentException e) {
			LOGGER.warn("Could not find a CommandEnum with value matching first field in [{}]", message);
			return null;
		}
	}
	
	public ConnectionStateMessage getConnectionState(Device device) {
		return deviceConnectionStateCache.get(device);
	}

	public void setConnectionState(Device device, ConnectionStateMessage connectionStateMessage) {
		deviceConnectionStateCache.put(device, connectionStateMessage);
	}

	public boolean isDeviceOnLine(Device device) {
		var stateMessage = getConnectionState(device);
		return stateMessage != null && stateMessage.getState() == ConnectionStateEnum.ONLINE;
	}
	
	public String getTopicMessage(String topic) {
		return topicMessageCache.get(topic);
	}
	
	/**
	 * Puts an entry only if topic is not in the map or if the message is not the same
	 * @param topic
	 * @param message
	 * @return true if duplicate entry
	 */
	public boolean setTopicMessage(String topic, String message) {
		var value = topicMessageCache.get(topic);
		if (value != null && value.equals(message)) {
			return true;
		} else {
			topicMessageCache.put(topic, message);
			return false;
		}
	}

	@PreDestroy
	public void terminate() {
		resultTopicCache.clear();
		deviceConnectionStateCache.clear();
		topicMessageCache.clear();
	}

	public void dumpCache() {
	    LOGGER.info("Dump of resultTopicCache:");
	    resultTopicCache.forEach((key, value) ->
	        LOGGER.info("key: [{}], value: [{}]", key, value)
	    );

	    LOGGER.info("Dump of deviceConnectionStateCache:");
	    deviceConnectionStateCache.forEach((key, value) ->
	        LOGGER.info("key: [{}], value: [{}]", key, value)
	    );

	    LOGGER.info("Dump of topicMessageCache:");
	    topicMessageCache.forEach((key, value) ->
	        LOGGER.info("key: [{}], value: [{}]", key, value)
	    );
	}

}
