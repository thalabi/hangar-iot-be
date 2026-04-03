package com.kerneldc.iot.mqtt.result.zigbee2mqtt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kerneldc.iot.mqtt.result.AbstractBaseResult;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StateResult extends AbstractBaseResult {

	private String state;
	private Integer linkquality;
	private Boolean occupancy;
	private Integer battery;
	@JsonProperty("battery_low")
	private Boolean batteryLow;
	private String illumination;
	@JsonProperty("occupancy_sensitivity")
	private String occupancySensitivity;
	
//	@JsonCreator // Need @JsonCreator because '@JsonProperty(required = true)' is only enforced during deserialization  
//    public StateResult(@JsonProperty(required = true) String state, Integer linkquality, Boolean occupancy) {
//        this.state = state;
//        this.linkquality = linkquality;
//        this.occupancy = occupancy;
//    }
}
