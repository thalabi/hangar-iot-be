package com.kerneldc.hangariot.mqtt.result.timer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kerneldc.hangariot.mqtt.command.Timer;
import com.kerneldc.hangariot.mqtt.result.AbstractBaseResult;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString(callSuper = true)
@JsonDeserialize(using = TimerResultDeserializer.class)
public class TimerResult extends AbstractBaseResult {

	private Timer timerXX;
}
