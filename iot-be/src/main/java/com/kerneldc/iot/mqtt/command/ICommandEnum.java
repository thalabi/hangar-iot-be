package com.kerneldc.iot.mqtt.command;

import com.kerneldc.iot.domain.enums.BridgeEnum;
import com.kerneldc.iot.mqtt.result.AbstractBaseResult;

public interface ICommandEnum {
	BridgeEnum handlesBridge ();
	Class<? extends AbstractBaseResult> getResultType();
}
