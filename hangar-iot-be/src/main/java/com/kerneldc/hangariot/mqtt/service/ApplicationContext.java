package com.kerneldc.hangariot.mqtt.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kerneldc.hangariot.domain.device.Device;
import com.kerneldc.hangariot.domain.enums.BridgeEnum;
import com.kerneldc.hangariot.mqtt.command.ICommandEnum;
import com.kerneldc.hangariot.mqtt.command.TasmotaCommandEnum;
import com.kerneldc.hangariot.mqtt.command.Zigbee2MqttCommandEnum;
import com.kerneldc.hangariot.mqtt.result.AbstractBaseResult;
import com.kerneldc.hangariot.mqtt.topic.TopicHelper;
import com.kerneldc.hangariot.websocket.ConnectionStateEnum;
import com.kerneldc.hangariot.websocket.message.ConnectionStateMessage;

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
	
	private record DeviceAndCommandEnum(Device device, ICommandEnum commandEnum) {}
	
	// Used to detect duplicates
//	private Map<String, String> topicMessageCache = new ConcurrentHashMap<>();

	/**
	 * @param topic
	 * @param message
	 * @return the stateResult as a json
	 * @throws JsonProcessingException
	 */
	public AbstractBaseResult setCommandResult(String topic, String message) throws JsonProcessingException {
		var device = topicHelper.getDevice(topic);
		ICommandEnum commandEnum; 
		if (device.getBridge() == BridgeEnum.ZIGBEE2MQTT) {
			commandEnum = Zigbee2MqttCommandEnum.STATE;
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

	public AbstractBaseResult getCommandResult(Device device, ICommandEnum commandEnum) {
		switch (commandEnum.handlesBridge()) {
		case ZIGBEE2MQTT: {
			return getZigbee2MqttStateResult(device);
		}
		case TASMOTA: {
			return resultTopicCache.get(new DeviceAndCommandEnum(device, commandEnum));
		}
		}
		return null;
	}

	public AbstractBaseResult getZigbee2MqttStateResult(Device device) {
		return resultTopicCache.get(new DeviceAndCommandEnum(device, Zigbee2MqttCommandEnum.STATE));
	}
	
	// Tasmota message
	private TasmotaCommandEnum getTasmotaCommandEnum(String message) throws JsonProcessingException {
		
		var jsonObject = objectMapper.readValue(message, ObjectNode.class);
		try {
			return TasmotaCommandEnum.valueOf(jsonObject.fieldNames().next().toUpperCase());
		} catch (IllegalArgumentException _) {
			LOGGER.warn("Could not find a TasmotaCommandEnum with value matching first field in [{}]", message);
			return null;
		}
	}
	
	public ConnectionStateMessage getConnectionState(Device device) {
		return deviceConnectionStateCache.get(device);
	}

	public void setConnectionState(Device device, ConnectionStateMessage connectionStateMessage) {
		deviceConnectionStateCache.put(device, connectionStateMessage);
	}

	public boolean isDeviceOffLine(Device device) {
		return ! /* not */ isDeviceOnLine(device);
	}
	
	public boolean isDeviceOnLine(Device device) {
		var stateMessage = getConnectionState(device);
		return stateMessage != null && stateMessage.getState() == ConnectionStateEnum.ONLINE;
	}
	
//	public String getTopicMessage(String topic) {
//		return topicMessageCache.get(topic);
//	}
	
	/**
	 * Puts an entry only if topic is not in the map or if the message is not the same
	 * @param topic
	 * @param message
	 * @return true if duplicate entry
	 */
//	public boolean setTopicMessage(String topic, String message) {
//		var value = topicMessageCache.get(topic);
//		if (value != null && value.equals(message)) {
//			return true;
//		} else {
//			topicMessageCache.put(topic, message);
//			return false;
//		}
//	}

	@PreDestroy
	public void terminate() {
		resultTopicCache.clear();
		deviceConnectionStateCache.clear();
//		topicMessageCache.clear();
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

//	    LOGGER.info("Dump of topicMessageCache:");
//	    topicMessageCache.forEach((key, value) ->
//	        LOGGER.info("key: [{}], value: [{}]", key, value)
//	    );
	}
	public ObjectNode dumpCacheToJson() {
		var applicationContextCache = objectMapper.createObjectNode();

		var resultTopicCacheEntries = objectMapper.createArrayNode();
	    resultTopicCache.forEach((key, value) -> {
	    	var resultTopicCacheEntry = objectMapper.createObjectNode();
	        resultTopicCacheEntry.put("deviceAndCommandEnum", key.toString());
	        StringUtils.uncapitalize(value.getClass().getSimpleName());
	        resultTopicCacheEntry.put(StringUtils.uncapitalize(value.getClass().getSimpleName()), value.toString());
	        resultTopicCacheEntries.add(resultTopicCacheEntry);
	    }
	    );
	    applicationContextCache.set("resultTopicCache", resultTopicCacheEntries);

	    
	    var deviceConnectionStateCacheEntries = objectMapper.createArrayNode();
	    deviceConnectionStateCache.forEach((key, value) -> {
	    	var deviceConnectionStateEntry = objectMapper.createObjectNode();
	        deviceConnectionStateEntry.put("device", key.toString());
	        deviceConnectionStateEntry.put("connectionStateMessage", value.toString());
	        deviceConnectionStateCacheEntries.add(deviceConnectionStateEntry);
	    }
	    );
	    applicationContextCache.set("deviceConnectionStateCache", deviceConnectionStateCacheEntries);
	    
	    
//	    var topicMessageCacheEntries = objectMapper.createArrayNode();
//	    topicMessageCache.forEach((key, value) -> {
//	    	var topicMessageCacheEntry = objectMapper.createObjectNode();
//	    	topicMessageCacheEntry.put("topic", key);
//	    	topicMessageCacheEntry.put("message", value);
//	    	topicMessageCacheEntries.add(topicMessageCacheEntry);
//	    }
//	    );
//	    applicationContextCache.set("topicMessageCache", topicMessageCacheEntries);
	    
	    return applicationContextCache;
	}

}
