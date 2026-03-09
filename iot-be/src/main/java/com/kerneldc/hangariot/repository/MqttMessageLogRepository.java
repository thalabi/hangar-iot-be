package com.kerneldc.hangariot.repository;

import com.kerneldc.hangariot.domain.enums.EntityEnum;
import com.kerneldc.hangariot.domain.enums.IEntityEnum;
import com.kerneldc.hangariot.domain.mqttmessagelog.MqttMessageLog;

public interface MqttMessageLogRepository extends BaseTableRepository<MqttMessageLog, Long>{

	default void persistMqttMessageLogSuccess(MqttMessageLog mqttMessageLog, long milliseconds) {
		persistMqttMessageLog(mqttMessageLog, milliseconds, true);
	}

	default void persistMqttMessageLogFailure(MqttMessageLog mqttMessageLog, long milliseconds) {
		persistMqttMessageLog(mqttMessageLog, milliseconds, false);
	}

	private void persistMqttMessageLog(MqttMessageLog mqttMessageLog, long milliseconds, boolean success) {
		mqttMessageLog.setCompletionTimeSeconds(milliseconds);
		mqttMessageLog.setSuccess(success);
		save(mqttMessageLog);
	}
	
	@Override
	default IEntityEnum canHandle() {
		return EntityEnum.MQTT_MESSAGE_LOG;
	}

}
