package com.kerneldc.iot.mqtt.result.tasmota.timer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kerneldc.iot.mqtt.result.AbstractBaseResult;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString(callSuper = true)
@JsonDeserialize(using = TimerResultDeserializer.class)
public class TimerResult extends AbstractBaseResult {

	private Timer timerXX;
}
