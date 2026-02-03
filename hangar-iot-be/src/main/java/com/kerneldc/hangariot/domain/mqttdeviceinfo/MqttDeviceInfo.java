package com.kerneldc.hangariot.domain.mqttdeviceinfo;

import java.time.OffsetDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.kerneldc.hangariot.domain.AbstractPersistableEntity;
import com.kerneldc.hangariot.domain.LogicalKeyHolder;
import com.kerneldc.hangariot.mqtt.result.zigbee2mqtt.MqttDeviceDetails;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class MqttDeviceInfo extends AbstractPersistableEntity {

	private static final long serialVersionUID = 1L;
	
	@Setter(AccessLevel.NONE)
	private String ieeeAddress;
	@JdbcTypeCode(SqlTypes.JSON)
	private MqttDeviceDetails deviceDetails;
	private OffsetDateTime timestamp;
	
	public void setIeeeAddress(String ieeeAddress) {
		this.ieeeAddress = ieeeAddress;
		setLogicalKeyHolder();
	}

	@Override
	protected void setLogicalKeyHolder() {
		var logicalKeyHolder = LogicalKeyHolder.build(ieeeAddress);
		super.setLogicalKeyHolder(logicalKeyHolder);
	}

}
