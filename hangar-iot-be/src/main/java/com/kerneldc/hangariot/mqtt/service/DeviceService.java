package com.kerneldc.hangariot.mqtt.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kerneldc.hangariot.domain.device.Device;
import com.kerneldc.hangariot.repository.DeviceRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {

	private final DeviceRepository deviceRepository;
	private List<Device> deviceList = new ArrayList<>();

	@Value("${client-exposed.mqtt.commands}")
	private String[] commands;

	@PostConstruct
	public void loadDevices() {
		LOGGER.info("Loading devices from database.");
		deviceList = deviceRepository.findAll();
		var i = 0;
		for (var device: deviceList) {
			var managedLabel = (BooleanUtils.isTrue(device.getIsManaged()) ? "Managed" : "Not managed");
			LOGGER.info("{} - [{}] ({}) ({})", String.format("%2d", ++i), device.getName(), device.getBridge(), managedLabel);
		}
	}

	public List<Device> getDeviceList() {
		return deviceList;
	}
	
	public Device getDevice(String name) {
		return deviceList.stream().filter(device -> StringUtils.equals(device.getName(), name)).findAny().orElse(null);
	}
	
	public List<String> getDeviceNameList() {
		return deviceList.stream().map(Device::getName).toList();
	}

	public List<String> getCommandList() {
		return Arrays.asList(commands);
	}
	
}
