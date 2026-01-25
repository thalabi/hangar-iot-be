package com.kerneldc.hangariot.repository;

import com.kerneldc.hangariot.domain.enums.EntityEnum;
import com.kerneldc.hangariot.domain.enums.IEntityEnum;
import com.kerneldc.hangariot.domain.zone.Zone;

public interface ZoneRepository extends BaseTableRepository<Zone, Long>{

	Zone findByName(String name);
	
	@Override
	default IEntityEnum canHandle() {
		return EntityEnum.ZONE;
	}

}
