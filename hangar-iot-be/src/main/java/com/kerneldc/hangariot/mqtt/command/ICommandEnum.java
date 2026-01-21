package com.kerneldc.hangariot.mqtt.command;

import com.kerneldc.hangariot.controller.Device.BridgeEnum;
import com.kerneldc.hangariot.mqtt.result.AbstractBaseResult;

public interface ICommandEnum {
	BridgeEnum handlesBridge ();
	Class<? extends AbstractBaseResult> getResultType();
}
