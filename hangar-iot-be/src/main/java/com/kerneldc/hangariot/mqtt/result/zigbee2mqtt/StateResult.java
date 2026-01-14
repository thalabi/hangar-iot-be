package com.kerneldc.hangariot.mqtt.result.zigbee2mqtt;

import com.kerneldc.hangariot.mqtt.result.AbstractBaseResult;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString(callSuper = true)
public class StateResult extends AbstractBaseResult {

	private String power;
}
