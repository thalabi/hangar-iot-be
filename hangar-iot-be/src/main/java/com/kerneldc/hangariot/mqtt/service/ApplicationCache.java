package com.kerneldc.hangariot.mqtt.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kerneldc.hangariot.mqtt.message.ConnectionStateEnum;
import com.kerneldc.hangariot.mqtt.message.StateMessage;
import com.kerneldc.hangariot.mqtt.result.AbstractBaseResult;
import com.kerneldc.hangariot.mqtt.result.CommandEnum;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationCache {
	
	private final ObjectMapper objectMapper;
	private Map<DeviceNameAndCommandEnum, AbstractBaseResult> resultTopicCache = Collections.synchronizedMap(new HashMap<>());
	private Map<String, StateMessage> deviceConnectionStateCache = new HashMap<>();
	
	private record DeviceNameAndCommandEnum(String deviceName, CommandEnum commandEnum) {}

	public void setCommandResult(String topic, String message) throws JsonProcessingException {
		var deviceName = extractDeviceName(topic);
	    var commandEnum = getCommandEnum(message);
	    if (commandEnum == null) {
	    	LOGGER.warn("Message type of [{}] is not supported. Can't add it to cache", message);
	    	return;
	    }
		var result = objectMapper.readValue(message, commandEnum.getResultType());
		resultTopicCache.put(new DeviceNameAndCommandEnum(deviceName, commandEnum), result);
	}

	public AbstractBaseResult getCommandResult(String deviceName, CommandEnum commandEnum) {
		return resultTopicCache.get(new DeviceNameAndCommandEnum(deviceName, commandEnum));
	}

	private CommandEnum getCommandEnum(String message) throws JsonProcessingException {
		var jsonObject = objectMapper.readValue(message, ObjectNode.class);
		try {
			return CommandEnum.valueOf(jsonObject.fieldNames().next().toUpperCase());
		} catch (IllegalArgumentException e) {
			LOGGER.warn("Could not find a CommandEnum with value matching first field in [{}]", message);
			return null;
		}
	}

	private String extractDeviceName(String topic) {
		var p = Pattern.compile(".+/(.+)/(RESULT|LwtMessage)");
		var m = p.matcher(topic);
		if (m.matches() && StringUtils.isNotEmpty(m.group(1))) {
			return m.group(1);
		} else {
			throw new IllegalStateException(String.format("Could not extract device name from topic [%s]", topic)); 
		}
	}

	public void dumpCache() {
		LOGGER.info("Dump of resultTopicCache:");
		for (var entry: resultTopicCache.entrySet()) {
			LOGGER.info("key: [{}], value: [{}]", entry.getKey(), entry.getValue());
		}
	}
	
	public StateMessage getConnectionState(String deviceName) {
		return deviceConnectionStateCache.get(deviceName);
	}

	public void setConnectionState(String deviceName, StateMessage stateMessage) {
		deviceConnectionStateCache.put(deviceName, stateMessage);
	}

	public boolean isDeviceOnLine(String deviceName) {
		var stateMessage = getConnectionState(deviceName);
		return stateMessage.getState() == ConnectionStateEnum.ONLINE;
	}
	
	
	@PreDestroy
	public void terminate() {
		resultTopicCache.clear();
		deviceConnectionStateCache.clear();
	}

	
}
