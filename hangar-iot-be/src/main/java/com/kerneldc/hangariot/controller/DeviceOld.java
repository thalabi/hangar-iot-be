package com.kerneldc.hangariot.controller;

import java.util.concurrent.locks.ReentrantLock;

import com.kerneldc.hangariot.domain.device.DeviceConfigData;
import com.kerneldc.hangariot.domain.enums.BridgeEnum;
import com.kerneldc.hangariot.domain.enums.DeviceTypeEnum;

import lombok.Data;

@Data
public class DeviceOld {
	
	private String name;
	private String description;
	private String location;
	private DeviceTypeEnum deviceType;
	private Boolean telemetry;
	private String make;
	private String iotDeviceModel;
	private Boolean enableDataSaver;
	private DeviceConfigData config;
	private DeviceGroupEnum group;
	private BridgeEnum bridge;
	private Boolean passive; // or non-reporting of their 'state'
	
	private ReentrantLock lock = new ReentrantLock();
}
