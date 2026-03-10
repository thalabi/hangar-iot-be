package com.kerneldc.iot.domain.area;

import com.kerneldc.iot.domain.AbstractPersistableEntity;
import com.kerneldc.iot.domain.LogicalKeyHolder;
import com.kerneldc.iot.domain.zone.Zone;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Area extends AbstractPersistableEntity {

	private static final long serialVersionUID = 1L;
	
	@Setter(AccessLevel.NONE)
	private String name;
    @ManyToOne
    @JoinColumn(name = "zone_id")
	private Zone zone;

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
