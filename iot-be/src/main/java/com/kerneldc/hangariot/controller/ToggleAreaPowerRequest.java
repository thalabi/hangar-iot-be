package com.kerneldc.hangariot.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data

public class ToggleAreaPowerRequest {

	@NotBlank(message = "Zone name is missing")
	private String zoneName;
	@NotBlank(message = "Area name is missing")
	private String areaName;
	@NotNull(message = "Requested power state is missing")
	private Boolean powerStateRequested;
}
