package com.kerneldc.iot.domain.deviceattributelog;

import java.time.OffsetDateTime;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.kerneldc.iot.domain.AbstractPersistableEntity;
import com.kerneldc.iot.domain.LogicalKeyHolder;
import com.kerneldc.iot.domain.device.Device;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class DeviceAttributeLog extends AbstractPersistableEntity {

	private static final long serialVersionUID = 1L;
	
	public record Change(
	        String path,
	        Object oldValue,
	        Object newValue
	) {}
	
	@Setter(AccessLevel.NONE)
	private OffsetDateTime timestamp;
	@JdbcTypeCode(SqlTypes.JSON)
	private List<Change> changes;
		
    @ManyToOne
    @JoinColumn(name = "device_id")
	private Device device;

	public void setTimestamp(OffsetDateTime timestamp) {
		this.timestamp = timestamp;
		setLogicalKeyHolder();
	}
	
	@Override
	protected void setLogicalKeyHolder() {
		var logicalKeyHolder = LogicalKeyHolder.build(timestamp, device.getId());
		super.setLogicalKeyHolder(logicalKeyHolder);
	}

}
