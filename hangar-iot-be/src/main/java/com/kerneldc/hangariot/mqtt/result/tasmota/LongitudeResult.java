package com.kerneldc.hangariot.mqtt.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString(callSuper = true)
public class LongitudeResult extends AbstractBaseResult {

	private Float longitude;
}
