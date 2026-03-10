package com.kerneldc.iot.mqtt.result.tasmota.timer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kerneldc.iot.mqtt.result.AbstractBaseResult;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString(callSuper = true)
//@JsonSerialize(using = TimersResultSerializer.class)
@JsonDeserialize(using = TimersResultDeserializer.class)

public class TimersResult extends AbstractBaseResult {

	private String timers; // ON or OFF
	private Timer[] timerArray = new Timer[16];
}
