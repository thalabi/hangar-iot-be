package com.kerneldc.hangariot.controller;


import com.kerneldc.hangariot.mqtt.command.Timer;
import com.kerneldc.hangariot.mqtt.result.AbstractBaseResult;

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
