package com.kerneldc.iot.domain.device;

import java.util.concurrent.locks.ReentrantLock;

import com.kerneldc.iot.domain.AbstractPersistableEntity;
import com.kerneldc.iot.domain.LogicalKeyHolder;
import com.kerneldc.iot.domain.area.Area;
import com.kerneldc.iot.domain.enums.BridgeEnum;
import com.kerneldc.iot.domain.enums.DeviceTypeEnum;
import com.kerneldc.iot.domain.zone.Zone;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Device extends AbstractPersistableEntity {

	private static final long serialVersionUID = 1L;
	
	@Setter(AccessLevel.NONE)
	private String name;
	// Ieee Address for Zigbee2mqtt devices and Id for ESPresense devices
	private String address;
    private String description;
	@Enumerated(EnumType.STRING)
    private DeviceTypeEnum deviceType;
    private Boolean telemetry;
    private String make;
    private String model;
    private Boolean enableDataSaver;
//    @Embedded
//    private DeviceConfig deviceConfig;
    private String location;
	@Enumerated(EnumType.STRING)
    private BridgeEnum bridge;
    private Boolean passive; // or non-reporting of their 'state'
    private Boolean isManaged;
	
    @Transient
    private ReentrantLock lock = new ReentrantLock();
    
    @ManyToOne
    @JoinColumn(name = "zone_id")
	private Zone zone;
    @ManyToOne
    @JoinColumn(name = "area_id")
	private Area area;

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
