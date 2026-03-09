package com.kerneldc.hangariot.controller;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TogglePowerRequest extends DeviceRequest {

	private String powerStateRequested;
}
