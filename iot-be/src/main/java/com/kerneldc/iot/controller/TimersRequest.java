package com.kerneldc.iot.controller;


import com.kerneldc.iot.mqtt.result.AbstractBaseResult;
import com.kerneldc.iot.mqtt.result.tasmota.timer.Timer;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)

public class TimersRequest extends AbstractBaseResult {

	@NotBlank(message = "Device name is missing")
	private String deviceName;
	
	private String timers; // ON or OFF
	private Boolean timersModified;

	private Timer[] timerArray = new Timer[16];
	private Boolean[] timerModifiedArray = new Boolean[16];
}
