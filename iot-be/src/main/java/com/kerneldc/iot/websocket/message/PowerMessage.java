package com.kerneldc.iot.websocket.message;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PowerMessage {
//	@JsonProperty("POWER")
	private String power;
	private Long timestamp;
}
