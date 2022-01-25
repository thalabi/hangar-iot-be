package com.kerneldc.hangariot.mqtt.result.timer;

import com.kerneldc.hangariot.mqtt.command.Timer;
import com.kerneldc.hangariot.mqtt.result.AbstractBaseResult;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString(callSuper = true)
public class Timer5Result extends AbstractBaseResult {

	private Timer timer5;
}
