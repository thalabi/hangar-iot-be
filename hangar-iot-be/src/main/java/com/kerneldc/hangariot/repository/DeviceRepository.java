package com.kerneldc.hangariot.repository;

import com.kerneldc.hangariot.domain.device.Device;
import com.kerneldc.hangariot.domain.enums.EntityEnum;
import com.kerneldc.hangariot.domain.enums.IEntityEnum;

public interface DeviceRepository extends BaseTableRepository<Device, Long>{

	Device findByName(String name);
	
	@Override
	default IEntityEnum canHandle() {
		return EntityEnum.AREA;
	}

}
