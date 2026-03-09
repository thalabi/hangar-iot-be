package com.kerneldc.hangariot.repository;

import java.util.List;

import com.kerneldc.hangariot.domain.device.Device;
import com.kerneldc.hangariot.domain.enums.EntityEnum;
import com.kerneldc.hangariot.domain.enums.IEntityEnum;

public interface DeviceRepository extends BaseTableRepository<Device, Long>{

	Device findByName(String name);
	List<Device> findByIsManaged(Boolean isManaged);
	
	@Override
	default IEntityEnum canHandle() {
		return EntityEnum.DEVICE;
	}

}
