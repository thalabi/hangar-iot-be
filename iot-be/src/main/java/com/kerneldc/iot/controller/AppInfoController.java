package com.kerneldc.iot.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kerneldc.iot.mqtt.service.ApplicationContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/appInfoController")
@RequiredArgsConstructor
@Slf4j
public class AppInfoController {

	private final ApplicationContext applicationContext;

	@Value("${build.version}")
	private String buildVersion;

	@Value("${build.timestamp}")
	private String buildTimestamp;

	@GetMapping("/getBuildInfo")
	public String getBuildInfo() {
		return buildVersion + "_" + buildTimestamp;
	}
	
    @GetMapping("/dumpCache")
	public ResponseEntity<Object> dumpCache() {
    	LOGGER.info("Begin ...");
//    	applicationContext.dumpCache();
    	var cacheDump = applicationContext.dumpCacheToJson();
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(cacheDump);
    }
    

}
