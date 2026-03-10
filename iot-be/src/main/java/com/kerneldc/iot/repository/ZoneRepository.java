package com.kerneldc.iot.repository;

import com.kerneldc.iot.domain.enums.EntityEnum;
import com.kerneldc.iot.domain.enums.IEntityEnum;
import com.kerneldc.iot.domain.zone.Zone;

public interface ZoneRepository extends BaseTableRepository<Zone, Long>{

	Zone findByName(String name);
	
	@Override
	default IEntityEnum canHandle() {
		return EntityEnum.ZONE;
	}

}
