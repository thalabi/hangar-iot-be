package com.kerneldc.iot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/protected/webSocketStatsConfigController")
@RequiredArgsConstructor
@Slf4j
public class WebSocketStatsConfigController {

	private final WebSocketMessageBrokerStats webSocketMessageBrokerStats;
	
    @PostMapping("/changeLoggingPeriod")
	public ResponseEntity<Void> changeLoggingPeriod(@Valid @RequestBody LoggingPeriodRequest loggingPeriodRequest) {
    	LOGGER.info("Begin ...");
    	LOGGER.info("Setting WebSocketMessageBrokerStats logging period to [{}] seconds.", loggingPeriodRequest.getLoggingPeriodSecs());
    	webSocketMessageBrokerStats.setLoggingPeriod(loggingPeriodRequest.getLoggingPeriodSecs() * 1000); // time in millis
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(null);
    }

}
