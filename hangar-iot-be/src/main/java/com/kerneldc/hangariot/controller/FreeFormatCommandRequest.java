package com.kerneldc.hangariot.controller;



import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FreeFormatCommandRequest {

	@NotBlank(message = "Device name is missing")
	private String deviceName;
	@NotBlank(message = "Command is missing")
	private String command;
	private String arguments;
}
