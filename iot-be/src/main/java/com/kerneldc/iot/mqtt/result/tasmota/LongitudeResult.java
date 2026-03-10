package com.kerneldc.iot.mqtt.result.tasmota;

import com.kerneldc.iot.mqtt.result.AbstractBaseResult;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString(callSuper = true)
public class LongitudeResult extends AbstractBaseResult {

	private Float longitude;
}
