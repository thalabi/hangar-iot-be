package com.kerneldc.hangariot.controller;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.kerneldc.hangariot.domain.device.Device;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DeviceResponse {

	@JsonUnwrapped
	private Device device;
}
