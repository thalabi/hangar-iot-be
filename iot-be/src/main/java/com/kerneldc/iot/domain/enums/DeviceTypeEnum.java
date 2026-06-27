package com.kerneldc.iot.domain.enums;

public enum DeviceTypeEnum {
	PLUG(new String[] { "state" }),
	WALL_SWITCH(new String[] { "state" }),
	MOTION_SENSOR(new String[] { "occupancy" }),
	BODY_SENSOR(new String[] { "occupancy", "illumination" }),
	ESPRESENSE_SENSOR(new String[] { "rssi", "distance" }),
	;
	
	String[] monitoredAttributes;
	
	public String[] getMonitoredAttributes() {
		return monitoredAttributes;
	}

	DeviceTypeEnum(String[] monitoredAttributes) {
		this.monitoredAttributes = monitoredAttributes;
	}
}
