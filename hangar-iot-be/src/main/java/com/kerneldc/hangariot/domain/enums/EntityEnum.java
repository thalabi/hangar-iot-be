package com.kerneldc.hangariot.domain.enums;

import java.util.Arrays;

import com.kerneldc.hangariot.domain.AbstractEntity;
import com.kerneldc.hangariot.domain.area.Area;
import com.kerneldc.hangariot.domain.device.Device;
import com.kerneldc.hangariot.domain.devicesinfo.DevicesInfo;
import com.kerneldc.hangariot.domain.zone.Zone;

public enum EntityEnum implements IEntityEnum {
	ZONE(Zone.class, false, new String[] {}),
	AREA(Area.class, false, new String[] {}),
	DEVICE(Device.class, false, new String[] {}),
	DEVICES_INFO(DevicesInfo.class, false, new String[] {}),
//	LOG_SHEET(LogSheet.class, false, new String[] {}),
//	JOURNEY_LOG(JourneyLog.class, false, new String[] {}),
//	ENGINE_LOG(EngineLog.class, false, new String[] {}),
//	TSN_V(TsnV.class, true, new String[] {}),
//	TSMOH_V(TsmohV.class, true, new String[] {}),
//	JOURNEY_LOG_V(JourneyLogV.class, true, new String[] {}),
//	ENGINE_LOG_V(EngineLogV.class, true, new String[] {}),
//	REMOTE_API_CALL_LOG(RemoteApiCallLog.class, false, new String[] {}),
//	REMOTE_API_CALL_DETAIL(RemoteApiCall.class, false, new String[] {})
	;

	Class<? extends AbstractEntity> entity;
	boolean immutable;
	String[] writeColumnOrder;

	EntityEnum(Class<? extends AbstractEntity> entity, boolean immutable) {
		this.entity = entity;
		this.immutable = immutable;
	}
	EntityEnum(Class<? extends AbstractEntity> entity, boolean immutable, String[] writeColumnOrder) {
		this.entity = entity;
		this.immutable = immutable;
		// tag SOURCECSVLINENUMBER to the end of the writeColumnOrder
		this.writeColumnOrder = Arrays.copyOf(writeColumnOrder, writeColumnOrder.length+1);
		this.writeColumnOrder[this.writeColumnOrder.length-1] = "SOURCECSVLINENUMBER";  
	}

	@Override
	public Class<? extends AbstractEntity> getEntity() {
		return entity;
	}

	@Override
	public boolean isImmutable() {
		return immutable;
	}

	@Override
	public String[] getWriteColumnOrder() {
		return writeColumnOrder;
	}

}
