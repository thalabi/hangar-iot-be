package com.kerneldc.iot.mqtt.service;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kerneldc.iot.domain.device.Device;
import com.kerneldc.iot.domain.deviceattributelog.DeviceAttributeLog.Change;
import com.kerneldc.iot.domain.enums.BridgeEnum;
import com.kerneldc.iot.mqtt.command.ICommandEnum;
import com.kerneldc.iot.mqtt.command.TasmotaCommandEnum;
import com.kerneldc.iot.mqtt.command.Zigbee2MqttCommandEnum;
import com.kerneldc.iot.mqtt.result.AbstractBaseResult;
import com.kerneldc.iot.mqtt.result.tasmota.PowerResult;
import com.kerneldc.iot.mqtt.result.zigbee2mqtt.StateResult;
import com.kerneldc.iot.mqtt.topic.TopicHelper;
import com.kerneldc.iot.util.TimeUtils;
import com.kerneldc.iot.websocket.ConnectionStateEnum;
import com.kerneldc.iot.websocket.message.ConnectionStateMessage;

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
	private Map<Device, ArrayDeque<TimestampAndChanges>> deviceAttributeChanges = new ConcurrentHashMap<>();
	
	private record DeviceAndCommandEnum(Device device, ICommandEnum commandEnum) {}
	protected record TimestampAndChanges(long timestamp, List<Change> changes) {}
	private static final int ATTRIBUTE_CHANGES_SIZE = 3;
	
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

	public StateResult getZigbee2MqttStateResult(Device device) {
		return (StateResult)resultTopicCache.get(new DeviceAndCommandEnum(device, Zigbee2MqttCommandEnum.STATE));
	}
	private PowerResult getTasmotaPowerResult(Device device) {
		return (PowerResult)resultTopicCache.get(new DeviceAndCommandEnum(device, TasmotaCommandEnum.POWER));
	}

	public boolean getPower(Device device) {
		switch (device.getBridge()) {
		case ZIGBEE2MQTT: {
			return StringUtils.equalsIgnoreCase(getZigbee2MqttStateResult(device).getState(), "on");
		}
		case TASMOTA: {
			return StringUtils.equalsIgnoreCase(getTasmotaPowerResult(device).getPower(), "on");
		}
		}
		throw new IllegalArgumentException();
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

	}
	
	public void pushChanges(Device device, long timestamp, List<Change> changes) {
		
		var changesDeque = deviceAttributeChanges.computeIfAbsent(device,
				_ -> new ArrayDeque<>(ATTRIBUTE_CHANGES_SIZE)); // Initialize
		
		// if timestamp is the same minute as last entry then merge the changes
		var lastEntry = changesDeque.peekLast();
		if (lastEntry != null && TimeUtils.isSameMinute(lastEntry.timestamp, timestamp)) {
			// Merge with existing minute
			changesDeque.pollLast(); // efficient way to remove last
			changesDeque.addLast(mergeEntries(lastEntry, changes));	
			return;
		}
		
		// Add as new entry, maintaining size limit
		if (changesDeque.size() == ATTRIBUTE_CHANGES_SIZE) {
			changesDeque.removeFirst();
		}
		
		changesDeque.addLast(new TimestampAndChanges(timestamp, changes));
	}
	private TimestampAndChanges mergeEntries(TimestampAndChanges existing, List<Change> newChanges) {
	    var combined = Stream.concat(existing.changes().stream(), newChanges.stream()).toList();
	    return new TimestampAndChanges(existing.timestamp(), combined);
	}
	
	public String getAttributeChanges(Device device) throws JsonProcessingException {
		var changesDeque = deviceAttributeChanges.get(device);
		if (changesDeque == null) {
			return StringUtils.EMPTY;
		}
		
		
		var changesJsonAll = objectMapper.writeValueAsString(changesDeque.reversed()); // In reverse order, ie latest first
		LOGGER.info("changesJsonAll [{}]", changesJsonAll);
		return changesJsonAll;		
	}

	protected Map<Device, ArrayDeque<TimestampAndChanges>> getDeviceAttributeChanges() {
		return deviceAttributeChanges;
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
	    
	    
	    var deviceAttributeChangesEntries = objectMapper.createArrayNode();
	    deviceAttributeChanges.forEach((key, value) -> {
	    	var deviceAttributeChangesEntry = objectMapper.createObjectNode();
	    	deviceAttributeChangesEntry.put("device", key.toString());
	    	deviceAttributeChangesEntry.put("attributeChanges", value.toString());
	    	deviceAttributeChangesEntries.add(deviceAttributeChangesEntry);
	    }
	    );
	    applicationContextCache.set("deviceAttributeChangesEntries", deviceAttributeChangesEntries);
	    
	    
	    return applicationContextCache;
	}

}
