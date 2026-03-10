package com.kerneldc.iot.controller;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PingResponse {

	private String message;
	private LocalDateTime timestamp;
}
