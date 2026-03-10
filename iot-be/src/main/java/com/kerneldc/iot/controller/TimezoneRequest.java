package com.kerneldc.iot.controller;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TimezoneRequest {

	@NotBlank(message = "Device name is missing")
	private String deviceName;
	@NotBlank(message = "Timezone offset is missing")
	@Pattern(regexp="([+|-])?(\\d{1,2})((:)(\\d\\d))?", message="Specify timezone using format -/+hh:mm")
	private String timezoneOffset;
}
