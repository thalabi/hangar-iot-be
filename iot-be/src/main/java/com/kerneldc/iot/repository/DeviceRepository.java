package com.kerneldc.iot.repository;

import java.util.List;

import com.kerneldc.iot.domain.device.Device;
import com.kerneldc.iot.domain.enums.EntityEnum;
import com.kerneldc.iot.domain.enums.IEntityEnum;

public interface DeviceRepository extends BaseTableRepository<Device, Long>{

	Device findByName(String name);
	List<Device> findByIsManaged(Boolean isManaged);
	
	@Override
	default IEntityEnum canHandle() {
		return EntityEnum.DEVICE;
	}

}
