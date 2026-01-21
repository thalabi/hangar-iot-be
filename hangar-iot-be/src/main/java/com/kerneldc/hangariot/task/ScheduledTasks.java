package com.kerneldc.hangariot.task;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kerneldc.hangariot.controller.Device;
import com.kerneldc.hangariot.exception.ApplicationException;
import com.kerneldc.hangariot.exception.DeviceOfflineException;
import com.kerneldc.hangariot.mqtt.result.tasmota.CommandEnum;
import com.kerneldc.hangariot.mqtt.service.DeviceService;
import com.kerneldc.hangariot.mqtt.service.MqttSenderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

	@Value("${telemetry.period.increase-to:1800}")
	private String increaseTelemetryPeriod;
	
	@Value("${telemetry.period.default:300}")
	private String decreaseTelemetryPeriod;
	
	private final DeviceService deviceService;
	private final MqttSenderService mqttSenderService;

	
	@Scheduled(cron = "${telemetry.scheduler.increase-task.cron-expression}")
	public void increaseTelemetryPeriod() throws ApplicationException, JsonProcessingException {
		var deviceList = deviceService.getDeviceList();
		for (Device device: deviceList) {
			if (Boolean.TRUE.equals(device.getEnableDataSaver())) {
				LOGGER.info("Increasing telePeriod for device [{}] to [{}]", device.getName(), increaseTelemetryPeriod);
				try {
					mqttSenderService.sendMessage(device, CommandEnum.TELEPERIOD, increaseTelemetryPeriod);
				} catch (DeviceOfflineException e) {
			    	LOGGER.error("Unable to increaseTelemetryPeriod for device {}", device.getName(),NestedExceptionUtils.getMostSpecificCause(e).getMessage());
				}
			}
		}
	}
	
	@Scheduled(cron = "${telemetry.scheduler.restore-task.cron-expression}")
	public void restoreTelemetryPeriod() throws InterruptedException, ApplicationException, JsonProcessingException {
		var deviceList = deviceService.getDeviceList();
		for (Device device: deviceList) {
			if (Boolean.TRUE.equals(device.getEnableDataSaver())) {
				LOGGER.info("Decreasing telePeriod for device [{}] to [{}]", device.getName(), decreaseTelemetryPeriod);
				try {
					mqttSenderService.sendMessage(device, CommandEnum.TELEPERIOD, decreaseTelemetryPeriod);
				} catch (DeviceOfflineException e) {
					LOGGER.error("Unable to restoreTelemetryPeriod for device {}", device.getName(),
							NestedExceptionUtils.getMostSpecificCause(e).getMessage());
				}
			}
		}
	}
}
