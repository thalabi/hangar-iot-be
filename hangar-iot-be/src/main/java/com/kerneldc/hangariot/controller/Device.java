package com.kerneldc.hangariot.controller;

import java.util.concurrent.locks.ReentrantLock;

import lombok.Data;

@Data
public class Device {

	public enum BridgeEnum {
		TASMOTA,
		ZIGBEE2MQTT
	};
	
	private enum DeviceTypeEnum {PLUG, MOTION_SENSOR}
	
	private String name;
	private String description;
	private String location;
	private DeviceTypeEnum deviceType;
	private Boolean telemetry;
	private String iotDeviceMake;
	private String iotDeviceModel;
	private Boolean enableDataSaver;
	private DeviceConfigData config;
	private DeviceGroupEnum group;
	private BridgeEnum bridge;
	private Boolean passive; // or non-reporting of their 'state'
	
	private ReentrantLock lock = new ReentrantLock();
}
