package com.kerneldc.iot.websocket.message;

import com.kerneldc.iot.websocket.ConnectionStateEnum;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConnectionStateMessage {
	private ConnectionStateEnum state;
	private Long timestamp;
}
