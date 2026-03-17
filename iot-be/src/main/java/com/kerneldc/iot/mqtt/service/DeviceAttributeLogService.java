package com.kerneldc.iot.mqtt.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kerneldc.iot.domain.device.Device;
import com.kerneldc.iot.domain.deviceattributelog.DeviceAttributeLog;
import com.kerneldc.iot.domain.deviceattributelog.DeviceAttributeLog.Change;
import com.kerneldc.iot.mqtt.result.AbstractBaseResult;
import com.kerneldc.iot.repository.DeviceAttributeLogRepository;
import com.kerneldc.iot.util.StateResultComparator;
import com.kerneldc.iot.util.TimeUtils;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceAttributeLogService {

	private final DeviceAttributeLogRepository deviceAttributeLogRepository;
	private final StateResultComparator stateResultComparator;


	public void logDiff(Device device, long timestamp, AbstractBaseResult oldState, AbstractBaseResult newState) {

		if (oldState == null) {
			return;
		}
		
		var changes = stateResultComparator.diff(oldState, newState, List.of(device.getDeviceType().getMonitoredAttributes()));
		
		if (changes.isEmpty()) {
			return;
		}
		
		save(device, timestamp, changes);
	}

	private void save(Device device, long timestamp, List<Change> changes) {
		var deviceAttributeLog = new DeviceAttributeLog();
		deviceAttributeLog.setDevice(device);
		deviceAttributeLog.setTimestamp(TimeUtils.epochMilliToOffsetDateTime(timestamp));
		deviceAttributeLog.setChanges(changes);
//		LOGGER.info("deviceAttributeLog.getChanges() [{}]", deviceAttributeLog.getChanges());
		deviceAttributeLogRepository.save(deviceAttributeLog);
	}
}
