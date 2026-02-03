package com.kerneldc.hangariot.mqtt.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
//	private List<Device> allDeviceList = new ArrayList<>();
	private List<Device> managedDeviceList = new ArrayList<>();

	@Value("${client-exposed.mqtt.commands}")
	private String[] commands;

	@PostConstruct
	public void loadDevices() {
		LOGGER.info("Loading devices from database.");
		var allDeviceList = deviceRepository.findAll();
//		allDeviceList = deviceRepository.findByIsManaged(true);
		var i = 0;
		for (var device: allDeviceList) {
			var managedLabel = (BooleanUtils.isTrue(device.getIsManaged()) ? "Managed" : "Not managed");
			LOGGER.info("{} - [{}] ({}) ({})", String.format("%2d", ++i), device.getName(), device.getBridge(), managedLabel);
//			LOGGER.info("{} - [{}] ({})", String.format("%2d", ++i), device.getName(), device.getBridge());
		}
		
		managedDeviceList = allDeviceList.stream().filter(device -> BooleanUtils.isTrue(device.getIsManaged()))
				.collect(Collectors.toList());

	}

//	public List<Device> getAllDeviceList() {
//		return allDeviceList;
//	}
	public List<Device> getManagedDeviceList() {
		return managedDeviceList;
	}
	
	public List<String> getManagedDeviceNameList() {
		return managedDeviceList.stream().map(Device::getName).toList();
	}
	
	public Device getDevice(String name) {
		return managedDeviceList.stream().filter(device -> StringUtils.equals(device.getName(), name)).findAny().orElse(null);
	}
	
	public List<String> getCommandList() {
		return Arrays.asList(commands);
	}
	
}
