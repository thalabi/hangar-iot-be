package com.kerneldc.hangariot.repository;

import com.kerneldc.hangariot.domain.devicesinfo.DevicesInfo;
import com.kerneldc.hangariot.domain.enums.EntityEnum;
import com.kerneldc.hangariot.domain.enums.IEntityEnum;

public interface DevicesInfoRepository extends BaseTableRepository<DevicesInfo, Long>{

	DevicesInfo findByKey(String key);
	
	@Override
	default IEntityEnum canHandle() {
		return EntityEnum.DEVICES_INFO;
	}

}
