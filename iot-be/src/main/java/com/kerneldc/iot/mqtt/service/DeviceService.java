package com.kerneldc.iot.mqtt.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kerneldc.iot.domain.area.Area;
import com.kerneldc.iot.domain.device.Device;
import com.kerneldc.iot.domain.zone.Zone;
import com.kerneldc.iot.exception.InvalidAreaException;
import com.kerneldc.iot.exception.InvalidDeviceException;
import com.kerneldc.iot.exception.InvalidZoneException;
import com.kerneldc.iot.repository.AreaRepository;
import com.kerneldc.iot.repository.DeviceRepository;
import com.kerneldc.iot.repository.ZoneRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {

	private final DeviceRepository deviceRepository;
	private final ZoneRepository zoneRepository;
	private final AreaRepository areaRepository;
	private List<Device> managedDeviceList = new ArrayList<>();
	private List<Zone> zoneList = new ArrayList<>();
	private List<Area> areaList = new ArrayList<>();

	@Value("${client-exposed.mqtt.commands}")
	private String[] commands;

	@PostConstruct
	public void init() {
		loadDevices();
		loadZonesAndAreas();
	}
	
	private void loadDevices() {
		LOGGER.info("Loading devices from database.");
		var allDeviceList = deviceRepository.findAll();
		var i = 0;
		for (var device: allDeviceList) {
			var managedLabel = (BooleanUtils.isTrue(device.getIsManaged()) ? "Managed" : "Not managed");
			LOGGER.info("{} - [{}] ({}) ({})", String.format("%2d", ++i), device.getName(), device.getBridge(), managedLabel);
		}
		
		managedDeviceList = allDeviceList.stream().filter(device -> BooleanUtils.isTrue(device.getIsManaged()))
				.collect(Collectors.toList());
	}

	private void loadZonesAndAreas() {
		LOGGER.info("Loading zones and areas from database.");
		zoneList = zoneRepository.findAll();
		areaList = areaRepository.findAll();
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
	
	public List<Device> findByZoneAndArea(String zoneName, String areaName, Boolean powerStateRequested) {
		return managedDeviceList.stream()
				.filter(device -> StringUtils.equals(device.getZone().getName(), zoneName)
						&& StringUtils.equals(device.getArea().getName(), areaName)
						)
				.toList();
	}
	
//	public List<Zone> getZoneList() {
//		return zoneList;
//	}
//	public List<Area> getAreaList() {
//		return areaList;
//	}
	
	private List<String> getZoneNameList() {
		return zoneList.stream().map(Zone::getName).toList();
	}
	private List<String> getAreaNameList() {
		return areaList.stream().map(Area::getName).toList();
	}

	
    public void validateDeviceName(String deviceName) throws InvalidDeviceException {
    	if (! /* not */ getManagedDeviceNameList().contains(deviceName)) {
    		throw new InvalidDeviceException(String.format("Device [%s] is invalid", deviceName));
    	}
    }

    public void validateZoneName(String zoneName) throws InvalidZoneException {
    	if (! /* not */ getZoneNameList().contains(zoneName)) {
    		throw new InvalidZoneException(String.format("Zone [%s] is invalid", zoneName));
    	}
    }
    public void validateAreaName(String areaName) throws InvalidAreaException {
    	if (! /* not */ getAreaNameList().contains(areaName)) {
    		throw new InvalidAreaException(String.format("Area [%s] is invalid", areaName));
    	}
    }
    
	
}
