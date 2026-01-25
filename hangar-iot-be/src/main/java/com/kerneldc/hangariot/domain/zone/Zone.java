package com.kerneldc.hangariot.domain.zone;

import com.kerneldc.hangariot.domain.AbstractPersistableEntity;
import com.kerneldc.hangariot.domain.LogicalKeyHolder;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Zone extends AbstractPersistableEntity {

	private static final long serialVersionUID = 1L;
	
	@Setter(AccessLevel.NONE)
	private String name;

	public void setName(String name) {
		this.name = name;
		setLogicalKeyHolder();
	}
	
	@Override
	protected void setLogicalKeyHolder() {
		var logicalKeyHolder = LogicalKeyHolder.build(name);
		super.setLogicalKeyHolder(logicalKeyHolder);
	}

}
