package com.kerneldc.hangariot.mqtt.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString(callSuper = true)
public class TimeResult extends AbstractBaseResult {

	private String time;
}
