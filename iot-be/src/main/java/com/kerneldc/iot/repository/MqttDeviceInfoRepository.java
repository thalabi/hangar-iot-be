package com.kerneldc.iot.repository;

import java.util.Optional;

import com.kerneldc.iot.domain.enums.EntityEnum;
import com.kerneldc.iot.domain.enums.IEntityEnum;
import com.kerneldc.iot.domain.mqttdeviceinfo.MqttDeviceInfo;

public interface MqttDeviceInfoRepository extends BaseTableRepository<MqttDeviceInfo, Long>{

	Optional<MqttDeviceInfo> findByIeeeAddress(String ieeeAddress);
	
	@Override
	default IEntityEnum canHandle() {
		return EntityEnum.MQTT_DEVICE_INFO;
	}

}
