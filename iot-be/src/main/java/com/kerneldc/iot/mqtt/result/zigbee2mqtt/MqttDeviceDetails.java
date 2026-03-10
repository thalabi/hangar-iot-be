package com.kerneldc.iot.mqtt.result.zigbee2mqtt;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Device details as read from zigbee2mqtt/bridge/devices topic
 */
@Getter @Setter
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MqttDeviceDetails {

    private String ieeeAddress;
    private String type;
    private String networkAddress;
    private Boolean supported;
    private Boolean disabled;
    private String friendlyName;
    private Map<String, Object> endpoints;
    
    private Definition definition;
    
 // Nested class for the definition object
    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Definition {
        private String description;
        private String model;
        private String vendor;
        private Boolean supportsOta;
        
        // Use generic lists/maps for deeply nested parts like 'exposes' or 'options'
        private List<Map<String, Object>> exposes;
        private List<Map<String, Object>> options;
    }
}
