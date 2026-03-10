package com.kerneldc.iot.controller;



import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceRequest {

	@NotBlank(message = "Device name is missing")
	private String deviceName;
}
