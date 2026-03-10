package com.kerneldc.iot.repository;

import com.kerneldc.iot.domain.area.Area;
import com.kerneldc.iot.domain.enums.EntityEnum;
import com.kerneldc.iot.domain.enums.IEntityEnum;

public interface AreaRepository extends BaseTableRepository<Area, Long>{

	Area findByName(String name);
	
	@Override
	default IEntityEnum canHandle() {
		return EntityEnum.AREA;
	}

}
