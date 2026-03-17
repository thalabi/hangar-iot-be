package com.kerneldc.iot.repository;

import com.kerneldc.iot.domain.deviceattributelog.DeviceAttributeLog;
import com.kerneldc.iot.domain.enums.EntityEnum;
import com.kerneldc.iot.domain.enums.IEntityEnum;

public interface DeviceAttributeLogRepository extends BaseTableRepository<DeviceAttributeLog, Long>{

//	Device findByName(String name);
//	List<Device> findByIsManaged(Boolean isManaged);
	
	@Override
	default IEntityEnum canHandle() {
		return EntityEnum.DEVICE_ATTRIBUTE_LOG;
	}

}
