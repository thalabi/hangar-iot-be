package com.kerneldc.iot.controller;



import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class LoggingPeriodRequest {

	@Positive
	private Long loggingPeriodSecs;
}
