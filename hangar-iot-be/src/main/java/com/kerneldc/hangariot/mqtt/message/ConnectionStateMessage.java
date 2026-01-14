package com.kerneldc.hangariot.mqtt.message;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StateMessage {
	private ConnectionStateEnum state;
	private Long timestamp;
}
