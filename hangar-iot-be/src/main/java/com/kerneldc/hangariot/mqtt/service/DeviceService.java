package com.kerneldc.hangariot.mqtt.service;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kerneldc.hangariot.domain.device.Device;
import com.kerneldc.hangariot.repository.DeviceRepository;
import com.kerneldc.hangariot.springconfig.DeviceListPropertyHolder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {

//	private final DeviceListPropertyHolder deviceListPropertyHolder;
	private final DeviceRepository deviceRepository;

	@Value("${client-exposed.mqtt.commands}")
	private String[] commands;
	@Value("${websocket.topics.prefix:/topic}")
	private String websocketTopicsPrefix;

	public List<Device> getDeviceList() {
		var devices = deviceRepository.findAll();
		devices.forEach(device -> System.out.println("====================================================="+device));
//		return deviceListPropertyHolder.getDeviceList();
		return deviceRepository.findAll();
	}
	
	public Device getDevice(String name) {
		return getDeviceList().stream().filter(device -> StringUtils.equals(device.getName(), name)).findAny().orElse(null);
	}
	
	public List<String> getDeviceNameList() {
//		return deviceListPropertyHolder.getDeviceList().stream().map(Device::getName).toList();
		return getDeviceList().stream().map(Device::getName).toList();
	}

	public List<String> getCommandList() {
		return Arrays.asList(commands);
	}
	
}
