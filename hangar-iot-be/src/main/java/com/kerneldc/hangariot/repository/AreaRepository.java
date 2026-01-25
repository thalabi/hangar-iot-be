package com.kerneldc.hangariot.repository;

import com.kerneldc.hangariot.domain.area.Area;
import com.kerneldc.hangariot.domain.enums.EntityEnum;
import com.kerneldc.hangariot.domain.enums.IEntityEnum;

public interface AreaRepository extends BaseTableRepository<Area, Long>{

	Area findByName(String name);
	
	@Override
	default IEntityEnum canHandle() {
		return EntityEnum.AREA;
	}

}
