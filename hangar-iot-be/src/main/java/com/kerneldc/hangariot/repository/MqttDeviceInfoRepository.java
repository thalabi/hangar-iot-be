package com.kerneldc.hangariot.repository;

import java.util.Optional;

import com.kerneldc.hangariot.domain.enums.EntityEnum;
import com.kerneldc.hangariot.domain.enums.IEntityEnum;
import com.kerneldc.hangariot.domain.mqttdeviceinfo.MqttDeviceInfo;

public interface MqttDeviceInfoRepository extends BaseTableRepository<MqttDeviceInfo, Long>{

	Optional<MqttDeviceInfo> findByIeeeAddress(String ieeeAddress);
	
	@Override
	default IEntityEnum canHandle() {
		return EntityEnum.MQTT_DEVICE_INFO;
	}

}
