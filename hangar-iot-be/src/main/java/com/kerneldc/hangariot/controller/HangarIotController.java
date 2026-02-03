package com.kerneldc.hangariot.controller;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kerneldc.hangariot.exception.ApplicationException;
import com.kerneldc.hangariot.exception.InvalidDeviceException;
import com.kerneldc.hangariot.mqtt.command.TasmotaCommandEnum;
import com.kerneldc.hangariot.mqtt.result.AbstractBaseResult;
import com.kerneldc.hangariot.mqtt.result.tasmota.timer.TimersResult;
import com.kerneldc.hangariot.mqtt.service.DeviceService;
import com.kerneldc.hangariot.mqtt.service.MqttSenderService;
import com.kerneldc.hangariot.mqtt.service.WebSocketSenderService;
import com.kerneldc.hangariot.task.ScheduledTasks;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/protected/hangarIotController")
@RequiredArgsConstructor
@Slf4j
public class HangarIotController {

	private final MqttSenderService mqttSenderService;
	private final WebSocketSenderService webSocketSenderService;
	private final DeviceService deviceService;
	private final ScheduledTasks scheduledTasks;
	
    @GetMapping("/ping")
	public ResponseEntity<PingResponse> ping() {
    	LOGGER.info("Begin ...");
    	PingResponse pingResponse = new PingResponse();
    	pingResponse.setMessage("pong");
    	pingResponse.setTimestamp(LocalDateTime.now());
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(pingResponse);
    }
    
    @PostMapping("/publishConnectionState")
    public ResponseEntity<String> publishConnectionState(@Valid @RequestBody DeviceRequest deviceRequest) throws ApplicationException {
    	LOGGER.info("Begin ...");
    	var deviceName = deviceRequest.getDeviceName();
    	validateDeviceName(deviceName);
    	
    	webSocketSenderService.publishConnectionState(deviceService.getDevice(deviceName));
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(StringUtils.EMPTY);
    }

    @PostMapping("/triggerPublishPowerState")
	public ResponseEntity<String> triggerPublishPowerState(@Valid @RequestBody DeviceRequest deviceRequest) throws JsonProcessingException, ApplicationException {
    	LOGGER.info("Begin ...");
    	var deviceName = deviceRequest.getDeviceName();
    	validateDeviceName(deviceName);
    	
		mqttSenderService.triggerPublishPowerState(deviceService.getDevice(deviceName));
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(StringUtils.EMPTY);
    }
    
    @PostMapping("/togglePower")
	public ResponseEntity<String> togglePower(@Valid @RequestBody TogglePowerRequest togglePowerRequest) throws ApplicationException {
    	LOGGER.info("Begin ...");
    	var deviceName = togglePowerRequest.getDeviceName();
    	validateDeviceName(deviceName);
    	
   		mqttSenderService.togglePower(deviceService.getDevice(deviceName), togglePowerRequest.getPowerStateRequested());
   		LOGGER.info("End ...");
    	return ResponseEntity.ok(StringUtils.EMPTY);
    }

    /*
     * Should be used by devices that support sensor data like the Sonoff S-31 
     */
    @PostMapping("/triggerPublishSensorData")
	public ResponseEntity<String> triggerPublishSensorData(@Valid @RequestBody DeviceRequest deviceRequest) throws ApplicationException {
    	LOGGER.info("Begin ...");
    	var deviceName = deviceRequest.getDeviceName();
    	validateDeviceName(deviceName);
   		mqttSenderService.triggerPublishSensorData(deviceService.getDevice(deviceName));
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(StringUtils.EMPTY);
    }
    
    @PostMapping("/triggerTimezoneValue")
	public ResponseEntity<String> triggerTimezoneValue(@Valid @RequestBody DeviceRequest deviceRequest) throws JsonProcessingException, ApplicationException {
    	LOGGER.info("Begin ...");
    	var deviceName = deviceRequest.getDeviceName();
    	validateDeviceName(deviceName);
    	
		mqttSenderService.triggerTimezoneValue(deviceService.getDevice(deviceName));
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(StringUtils.EMPTY);
    }
    
    @PostMapping("/setTelePeriod")
	public ResponseEntity<String> setTelePeriod(@Valid @RequestBody TelePeriodRequest timezoneRequest) throws ApplicationException {
    	LOGGER.info("Begin ...");
    	var deviceName = timezoneRequest.getDeviceName();
    	validateDeviceName(deviceName);
    	
		mqttSenderService.setTelePeriod(deviceService.getDevice(deviceName), timezoneRequest.getTelePeriod());
    	
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(StringUtils.EMPTY);
    }
    
    @PostMapping("/setTimezoneOffset")
	public ResponseEntity<String> setTimezoneOffset(@Valid @RequestBody TimezoneRequest timezoneRequest) throws ApplicationException {
    	LOGGER.info("Begin ...");
    	var deviceName = timezoneRequest.getDeviceName();
    	validateDeviceName(deviceName);
		mqttSenderService.setTimezoneOffset(deviceService.getDevice(deviceName), timezoneRequest.getTimezoneOffset());
    	
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(StringUtils.EMPTY);
    }
    
    // TODO not fully coded
    @PostMapping("/setTimeStd")
	public ResponseEntity<String> setsetTimeStd(@Valid @RequestBody TimeStdRequest timeStdRequest) throws ApplicationException {
    	LOGGER.info("Begin ...");
    	var deviceName = timeStdRequest.getDeviceName();
    	validateDeviceName(deviceName);

//    	try {
			mqttSenderService.setTimeStdt(deviceName, timeStdRequest);
//		} catch (ApplicationException e) {
//			e.printStackTrace();
//			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(NestedExceptionUtils.getMostSpecificCause(e).getMessage());
//		}
    	
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(StringUtils.EMPTY);
    }
    
    @GetMapping("/getTimers")
    public ResponseEntity<TimersResult> getTimers(@Valid String deviceName) throws JsonProcessingException, ApplicationException {
    	LOGGER.info("Begin ...");
    	validateDeviceName(deviceName);
    	
		var	result = mqttSenderService.getTimers(deviceService.getDevice(deviceName));

		LOGGER.info("End ...");
    	return ResponseEntity.ok(result);
    }

    @PostMapping("/setTimers")
    public ResponseEntity<String> setTimers(@Valid @RequestBody TimersRequest timersRequest) throws JsonProcessingException, ApplicationException {    	
    	LOGGER.info("Begin ...");
    	var deviceName = timersRequest.getDeviceName();
    	validateDeviceName(deviceName);

		mqttSenderService.setTimers(timersRequest);
    	
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(StringUtils.EMPTY);
    }

    @PostMapping("/executeFreeFormatCommand")
	public ResponseEntity<AbstractBaseResult> executeFreeFormatCommand(@Valid @RequestBody FreeFormatCommandRequest freeFormatCommandRequest) throws ApplicationException, JsonProcessingException {
    	LOGGER.info("Begin ...");
    	var deviceName = freeFormatCommandRequest.getDeviceName();
    	validateDeviceName(deviceName);

    	var commandEnum = TasmotaCommandEnum.valueOf(freeFormatCommandRequest.getCommand().toUpperCase());
    	var abstractBaseResult = mqttSenderService.sendMessage(deviceService.getDevice(deviceName), commandEnum,
					freeFormatCommandRequest.getArguments());
		LOGGER.info("abstractBaseResult: [{}]", abstractBaseResult);
		var result = commandEnum.getResultType().cast(abstractBaseResult);
		
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(result);
    }
    
    @GetMapping("/getDeviceList")
	public ResponseEntity<List<DeviceResponse>> getDeviceList() {
    	LOGGER.info("Begin ...");
    	var authorizedDeviceNames = getAuthorizedDeviceNames();
    	LOGGER.info("authorizedDeviceNames: [{}]", authorizedDeviceNames);
    	var deviceResponseList = deviceService.getManagedDeviceList().stream().filter(device -> authorizedDeviceNames.contains(device.getName())).map(device -> {
    		var deviceResponse = new DeviceResponse();
    		deviceResponse.setDevice(device);
    		return deviceResponse;
    	}).toList();

    	LOGGER.info("End ...");
    	return ResponseEntity.ok(deviceResponseList);
    }
    
    @GetMapping("/getCommandList")
	public ResponseEntity<List<CommandResponse>> getCommandList() {
    	LOGGER.info("Begin ...");
    	var commandList = deviceService.getCommandList().stream().map(command -> {
    		var commandResponse = new CommandResponse();
    		commandResponse.setCommand(command);
    		return commandResponse;
    	}).toList();
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(commandList);
    }
    
    @PostMapping("/increaseTelemetryPeriod")
	public ResponseEntity<Void> increaseTelemetryPeriod() throws ApplicationException, JsonProcessingException {
    	LOGGER.info("Begin ...");
    	scheduledTasks.increaseTelemetryPeriod();
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(null);
    }

    private void validateDeviceName(String deviceName) throws InvalidDeviceException {
    	if (! /* not */ deviceService.getManagedDeviceNameList().contains(deviceName)) {
    		throw new InvalidDeviceException(String.format("Device [%s] is invalid", deviceName));
    	}
    }

    private Authentication getAuthentication() {
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	if (!(authentication instanceof AnonymousAuthenticationToken)) {
    	    LOGGER.info("authentication:  [{}]", authentication);
    	    return authentication;
    	} else {
    		return null;
    	}
    }
    private List<String> getAuthorizedDeviceNames() {
    	var authentication = getAuthentication();
    	if (authentication != null) {
    		return authentication.getAuthorities().stream().filter(grantedAuthority -> grantedAuthority.getAuthority().startsWith("ROLE_realm_device:")).
    		map(grantedAuthority -> grantedAuthority.getAuthority().substring(18)).toList();
    	} else {
    		return Collections.emptyList();
    	}
    }
}
