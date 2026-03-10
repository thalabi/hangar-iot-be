package com.kerneldc.iot.mqtt.service;

import org.springframework.stereotype.Service;

import com.kerneldc.iot.domain.mqttdeviceinfo.MqttDeviceInfo;
import com.kerneldc.iot.repository.MqttDeviceInfoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MqttDeviceInfoService {

	private final MqttDeviceInfoRepository mqttDeviceInfoRepository;
	
	@Transactional
	public MqttDeviceInfo saveOrUpdate(MqttDeviceInfo incoming) {

	    return mqttDeviceInfoRepository.findByIeeeAddress(incoming.getIeeeAddress())
	        .map(existing -> {
	            existing.setDeviceDetails(incoming.getDeviceDetails());
	            existing.setTimestamp(incoming.getTimestamp());
	            return mqttDeviceInfoRepository.save(existing); // update
	        })
	        .orElseGet(() -> mqttDeviceInfoRepository.save(incoming)); // insert
	}

}
