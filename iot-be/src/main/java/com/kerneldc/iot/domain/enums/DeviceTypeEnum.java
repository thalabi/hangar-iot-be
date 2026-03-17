package com.kerneldc.iot.domain.enums;

public enum DeviceTypeEnum {
	PLUG(new String[] { "linkquality", "state" }),
	WALL_SWITCH(new String[] { "linkquality", "state" }),
	MOTION_SENSOR(new String[] { "linkquality", "occupancy" }),
	BODY_SENSOR(new String[] { "linkquality", "occupancy", "illumination" }),
	;
	
	String[] monitoredAttributes;
	
	public String[] getMonitoredAttributes() {
		return monitoredAttributes;
	}

	DeviceTypeEnum(String[] monitoredAttributes) {
		this.monitoredAttributes = monitoredAttributes;
	}
}
