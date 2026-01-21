package com.kerneldc.hangariot.mqtt.command;

import org.apache.commons.lang3.StringUtils;

import com.kerneldc.hangariot.controller.Device.BridgeEnum;
import com.kerneldc.hangariot.mqtt.result.AbstractBaseResult;
import com.kerneldc.hangariot.mqtt.result.zigbee2mqtt.StateResult;


public enum Zigbee2MqttCommandEnum implements ICommandEnum {
	STATE(StringUtils.EMPTY, StringUtils.EMPTY, StateResult.class),
	GET_STATE("/get","{\"state\": \"\"}", StateResult.class),
	TOGGLE_POWER("/set","{\"state\": \"toggle\"}", StateResult.class);

	String subTopic;
	String payload;
	Class<? extends AbstractBaseResult> resultType;

	Zigbee2MqttCommandEnum(String subTopic, String payload, Class<? extends AbstractBaseResult> resultType) {
		this.subTopic = subTopic;
		this.payload = payload;
		this.resultType = resultType;
	}

	public String getSubTopic() {
		return subTopic;
	}

	public String getPayload() {
		return payload;
	}

	@Override
	public Class<? extends AbstractBaseResult> getResultType() {
		return resultType;
	}

	@Override
	public BridgeEnum handlesBridge () {
		return BridgeEnum.ZIGBEE2MQTT;
	}

}
