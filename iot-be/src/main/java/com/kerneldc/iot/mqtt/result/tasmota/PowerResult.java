package com.kerneldc.iot.mqtt.result.tasmota;

import com.kerneldc.iot.mqtt.result.AbstractBaseResult;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString(callSuper = true)
public class PowerResult extends AbstractBaseResult {

	private String power;
}
