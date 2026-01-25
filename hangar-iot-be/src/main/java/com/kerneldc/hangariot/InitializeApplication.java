package com.kerneldc.hangariot;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.kerneldc.hangariot.domain.enums.BridgeEnum;
import com.kerneldc.hangariot.mqtt.service.ApplicationContext;
import com.kerneldc.hangariot.mqtt.service.DeviceService;
import com.kerneldc.hangariot.mqtt.service.MqttSenderService;
import com.kerneldc.hangariot.websocket.ConnectionStateEnum;
import com.kerneldc.hangariot.websocket.message.ConnectionStateMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class InitializeApplication implements ApplicationRunner {

	private final MqttSenderService mqttSenderService;
	private final DeviceService deviceService;
	private final ApplicationContext applicationContext;
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		
		var deviceList = deviceService.getDeviceNameList();
		
		LOGGER.info("Managing devices: {}", String.join(", ", deviceList));
		var i = 0;
		for (var device: deviceService.getDeviceList()) {
			LOGGER.info("{} - device [{}] ({})", String.format("%2d", ++i), device.getName(), device.getBridge());
		}
		
		connectionStateOfZ2mDevices();
	}

	private void connectionStateOfZ2mDevices() {
		LOGGER.info("Finding out the connection state of Zigbee2Mqtt devices:");
		var i = 0;
		for (var device: deviceService.getDeviceList()) {
			if (device.getBridge() == BridgeEnum.ZIGBEE2MQTT) {
				LOGGER.info("{} - device [{}]", String.format("%2d", ++i), device.getName());
				if (BooleanUtils.isTrue(device.getPassive())) {
					applicationContext.setConnectionState(device, new ConnectionStateMessage(ConnectionStateEnum.PASSIVE, System.currentTimeMillis()));
				} else {
					mqttSenderService.triggerPublishConnectionState(device);
				}
			}
		}
	}

}
