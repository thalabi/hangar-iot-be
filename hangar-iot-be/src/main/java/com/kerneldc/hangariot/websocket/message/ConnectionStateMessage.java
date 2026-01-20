package com.kerneldc.hangariot.websocket.message;

import com.kerneldc.hangariot.websocket.ConnectionStateEnum;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConnectionStateMessage {
	private ConnectionStateEnum state;
	private Long timestamp;
}
