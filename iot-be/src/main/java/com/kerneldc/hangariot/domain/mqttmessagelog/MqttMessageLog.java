package com.kerneldc.hangariot.domain.mqttmessagelog;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.apache.commons.lang3.StringUtils;

import com.kerneldc.hangariot.domain.AbstractPersistableEntity;
import com.kerneldc.hangariot.domain.LogicalKeyHolder;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class MqttMessageLog extends AbstractPersistableEntity {

	private static final long serialVersionUID = 1L;
	
	@Setter(AccessLevel.NONE)
	private OffsetDateTime timestamp;
	@Setter(AccessLevel.NONE)
	private String topic;

	@Setter(AccessLevel.NONE)
	private String message;

	private Long completionTimeSeconds;
	private Boolean success;
	
	public void setMessage(String message) {
		// Replaces \r and \n with nothing; null-safe by default
	    this.message = StringUtils.replaceChars(message, "\r\n", null);
	}

	public void setTimestamp(OffsetDateTime timestamp) {
		this.timestamp = timestamp;
		setLogicalKeyHolder();
	}
	
	public void setTopic(String topic) {
		this.topic = topic;
		setLogicalKeyHolder();
	}

	@Override
	protected void setLogicalKeyHolder() {
		var logicalKeyHolder = LogicalKeyHolder.build(timestamp, topic);
		super.setLogicalKeyHolder(logicalKeyHolder);
	}

	public static MqttMessageLog buildMqttMessageLog(long timestamp, String topic, String message) {
		var mqttMessageLog = new MqttMessageLog();
		mqttMessageLog.setTimestamp(fromEpoch(timestamp));
		mqttMessageLog.setTopic(topic);
		mqttMessageLog.setMessage(message);
		return mqttMessageLog;
	}
	private static OffsetDateTime fromEpoch(long epochMilli) {
		Instant instant = Instant.ofEpochMilli(epochMilli);
		return OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
	}

}
