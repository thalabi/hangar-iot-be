package com.kerneldc.hangariot.mqtt.result.zigbee2mqtt;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Device details as read from zigbee2mqtt/bridge/devices topic
 */
@Getter @Setter
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MqttDeviceDetails {

	@JsonProperty("ieee_address")
    private String ieeeAddress;
    private String type;
    @JsonProperty("network_address")
    private String networkAddress;
    private Boolean supported;
    private Boolean disabled;
    @JsonProperty("friendly_name")
    private String friendlyName;
    private Map<String, Object> endpoints;
}
