package com.kerneldc.iot.controller;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.kerneldc.iot.domain.device.Device;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DeviceResponse {

	@JsonUnwrapped
	private Device device;
}
