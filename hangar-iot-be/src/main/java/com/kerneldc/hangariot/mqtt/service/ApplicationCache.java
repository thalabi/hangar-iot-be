package com.kerneldc.hangariot.mqtt.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kerneldc.hangariot.mqtt.message.ConnectionStateEnum;
import com.kerneldc.hangariot.mqtt.message.ConnectionStateMessage;
import com.kerneldc.hangariot.mqtt.result.AbstractBaseResult;
import com.kerneldc.hangariot.mqtt.result.tasmota.CommandEnum;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationCache {
	
	private final ObjectMapper objectMapper;
	private Map<DeviceNameAndCommandEnum, AbstractBaseResult> resultTopicCache = new ConcurrentHashMap<>();
	private Map<String, ConnectionStateMessage> deviceConnectionStateCache = new ConcurrentHashMap<>();
	
	private record DeviceNameAndCommandEnum(String deviceName, CommandEnum commandEnum) {}
	
	private Map<String, String> topicMessageCache = new ConcurrentHashMap<>();

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

	private static final Pattern TOPIC_PATTERN = Pattern.compile(".+/(.+)/(RESULT|LwtMessage)");

	private String extractDeviceName(String topic) {
		var m = TOPIC_PATTERN.matcher(topic);
		if (m.matches() && StringUtils.isNotEmpty(m.group(1))) {
			return m.group(1);
		} else {
			throw new IllegalStateException(String.format("Could not extract device name from topic [%s]", topic)); 
		}
	}

	public void dumpCache() {
	    LOGGER.info("Dump of resultTopicCache:");
	    resultTopicCache.forEach((key, value) ->
	        LOGGER.info("key: [{}], value: [{}]", key, value)
	    );
	}

	
	public ConnectionStateMessage getConnectionState(String deviceName) {
		return deviceConnectionStateCache.get(deviceName);
	}

	public void setConnectionState(String deviceName, ConnectionStateMessage connectionStateMessage) {
		deviceConnectionStateCache.put(deviceName, connectionStateMessage);
	}

	public boolean isDeviceOnLine(String deviceName) {
		var stateMessage = getConnectionState(deviceName);
		return stateMessage != null && stateMessage.getState() == ConnectionStateEnum.ONLINE;
	}
	
	public String getTopicMessage(String topic) {
		return topicMessageCache.get(topic);
	}
	
	/**
	 * Puts an entry only if topic is not in the map or if the message is not the same
	 * @param topic
	 * @param message
	 * @return true only if entry is made
	 */
	public boolean setTopicMessage(String topic, String message) {
		var value = topicMessageCache.get(topic);
		if (value != null && value.equals(message)) {
			return false;
		} else {
			topicMessageCache.put(topic, message);
			return true;
		}
	}

	@PreDestroy
	public void terminate() {
		resultTopicCache.clear();
		deviceConnectionStateCache.clear();
		topicMessageCache.clear();
	}

	
}
