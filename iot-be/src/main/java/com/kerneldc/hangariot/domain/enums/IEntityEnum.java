package com.kerneldc.hangariot.domain.enums;

import com.kerneldc.hangariot.domain.AbstractEntity;

public interface IEntityEnum {

	Class<? extends AbstractEntity> getEntity();
	boolean isImmutable();
	String[] getWriteColumnOrder();
}