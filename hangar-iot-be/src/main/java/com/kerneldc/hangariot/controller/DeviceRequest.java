package com.kerneldc.hangariot.controller;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class DeviceRequest {

	@NotBlank(message = "Device name is missing")
	private String deviceName;
}
