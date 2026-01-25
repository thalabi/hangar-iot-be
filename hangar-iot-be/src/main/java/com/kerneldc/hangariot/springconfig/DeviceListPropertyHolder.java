package com.kerneldc.hangariot.springconfig;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.kerneldc.hangariot.domain.device.Device;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter @Setter
@ConfigurationProperties(prefix = "esp")
public class DeviceListPropertyHolder {
	private List<Device> deviceList = new ArrayList<>();
}
