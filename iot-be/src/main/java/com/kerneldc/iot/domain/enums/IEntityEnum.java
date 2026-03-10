package com.kerneldc.iot.domain.enums;

import com.kerneldc.iot.domain.AbstractEntity;

public interface IEntityEnum {

	Class<? extends AbstractEntity> getEntity();
	boolean isImmutable();
	String[] getWriteColumnOrder();
}