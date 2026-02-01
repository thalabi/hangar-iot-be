package com.kerneldc.hangariot.domain.devicesinfo;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.kerneldc.hangariot.domain.AbstractPersistableEntity;
import com.kerneldc.hangariot.domain.LogicalKeyHolder;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class DevicesInfo extends AbstractPersistableEntity {

	private static final long serialVersionUID = 1L;
	public static final String KEY = "DEVICES_INFO";
	
	private String key;
	@JdbcTypeCode(SqlTypes.JSON)
	private String devicesInfo;

	@Override
	protected void setLogicalKeyHolder() {
		var logicalKeyHolder = LogicalKeyHolder.build(key);
		super.setLogicalKeyHolder(logicalKeyHolder);
	}

}
