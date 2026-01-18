package com.kerneldc.hangariot.mqtt.result.zigbee2mqtt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kerneldc.hangariot.mqtt.result.AbstractBaseResult;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StateResult extends AbstractBaseResult {

	private String state;
	
	@JsonCreator
    public StateResult(@JsonProperty(value = "state", required = true) String state) {
        this.state = state;
    }
}
