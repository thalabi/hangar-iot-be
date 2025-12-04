package com.kerneldc.hangariot.mqtt.service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.hangariot.controller.TimeStdRequest;
import com.kerneldc.hangariot.controller.TimersRequest;
import com.kerneldc.hangariot.exception.ApplicationException;
import com.kerneldc.hangariot.exception.DeviceOfflineException;
import com.kerneldc.hangariot.exception.UnexpectedCommandResultException;
import com.kerneldc.hangariot.mqtt.message.ConnectionStateEnum;
import com.kerneldc.hangariot.mqtt.message.StateMessage;
import com.kerneldc.hangariot.mqtt.result.AbstractBaseResult;
import com.kerneldc.hangariot.mqtt.result.CommandEnum;
import com.kerneldc.hangariot.mqtt.result.PowerResult;
import com.kerneldc.hangariot.mqtt.result.TelePeriodResult;
import com.kerneldc.hangariot.mqtt.result.TimezoneResult;
import com.kerneldc.hangariot.mqtt.result.timer.TimerResult;
import com.kerneldc.hangariot.mqtt.result.timer.TimersResult;
import com.kerneldc.hangariot.mqtt.topic.TopicHelper;
import com.kerneldc.hangariot.springconfig.MqttConfig.MessageSender;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SenderService {

	// MQTT messages
	private final MessageSender messageSender;
	// WebSocket messages
	private final SimpMessagingTemplate webSocket;
	private final TopicHelper topicHelper;
	private final ApplicationCache applicationCache;
	private final ObjectMapper objectMapper;
	private final DeviceService deviceService;
	
	@Value("${command.execution.timeout:5}")
	private Integer commandExecutionTimeout;

	@Value("${websocket.topics.prefix:/topic}")
	private String websocketTopicsPrefix;

	private static final String UNEXPECTED_RESULT_MESSAGE_FORMAT = "Executing [%s] command with argument [%s] failed. Result came back as [%s], expected [%s]";

	public void togglePower(String device, String powerStateExpected) throws InterruptedException, ApplicationException, DeviceOfflineException {
		var result = (PowerResult)executeCommand(device, CommandEnum.POWER, "2"); // 2 toggles power
		if (! /* not */ StringUtils.equalsIgnoreCase(powerStateExpected, result.getPower())) {
			throw new ApplicationException(String.format(UNEXPECTED_RESULT_MESSAGE_FORMAT, CommandEnum.POWER, "2", result.getPower(), powerStateExpected));
		}
	}

	public void triggerPublishPowerState(String device) throws InterruptedException, DeviceOfflineException {
		executeCommand(device, CommandEnum.POWER);
	}

	public void triggerPublishSensorData(String device) throws InterruptedException, ApplicationException {
		checkDeviceOnline(device);
		// issue the command without an argument to get the teleperiod value
		var result = (TelePeriodResult)executeCommand(device, CommandEnum.TELEPERIOD);
		// issue the command again with the retrieved argument to trigger an update on the SENSOR topic
		var result2 = (TelePeriodResult)executeCommand(device, CommandEnum.TELEPERIOD, String.valueOf(result.getTelePeriod()));
		if (! /* not */ result.getTelePeriod().equals(result2.getTelePeriod())) {
			throw new UnexpectedCommandResultException(String.format(UNEXPECTED_RESULT_MESSAGE_FORMAT, CommandEnum.TELEPERIOD, result.getTelePeriod(), result2.getTelePeriod(), result.getTelePeriod()));
		}
	}


	private void checkDeviceOnline(String deviceName) throws DeviceOfflineException {
//		if (! /* not */ applicationCache.isDeviceOnLine(deviceName)) {
		if (! /* not */ applicationCache.isDeviceOnLine(deviceName)) {
			throw new DeviceOfflineException();
		}
	}

	public void triggerTimezoneValue(String device) throws InterruptedException, DeviceOfflineException {
		executeCommand(device, CommandEnum.TIMEZONE);
	}

	public void setTelePeriod(String device, String telePeriod) throws InterruptedException, ApplicationException, DeviceOfflineException {
		var result = (TelePeriodResult)executeCommand(device, CommandEnum.TELEPERIOD, telePeriod);
		if (! /* not */ applicationCache.isDeviceOnLine(device)) {			
			return;
		}
		if (! /* not */ result.getTelePeriod().equals(Integer.valueOf(telePeriod))) {
			throw new ApplicationException(String.format(UNEXPECTED_RESULT_MESSAGE_FORMAT, CommandEnum.TELEPERIOD, telePeriod, result.getTelePeriod(), telePeriod));			
		}
	}

	public void setTimezoneOffset(String device, String timezoneOffset) throws InterruptedException, ApplicationException, DeviceOfflineException {
		var result = (TimezoneResult)executeCommand(device, CommandEnum.TIMEZONE, timezoneOffset);
		if (! /* not */ applicationCache.isDeviceOnLine(device)) {
			return;
		}
		if (! /* not */ StringUtils.equals(timezoneOffset, "99") && ! /* not */ StringUtils.contains(timezoneOffset, ":")) {
			timezoneOffset += ":00";
		}
		// strip strings from colon and convert to integer for easier comparison  
		var resultTimezone = Integer.valueOf(result.getTimezone().replace(":", StringUtils.EMPTY));
		var expectedTimezone = Integer.valueOf(timezoneOffset.replace(":", StringUtils.EMPTY));
		if (! /* not */ resultTimezone.equals(expectedTimezone)) {
			throw new ApplicationException(String.format(UNEXPECTED_RESULT_MESSAGE_FORMAT, CommandEnum.TIMEZONE, timezoneOffset, result.getTimezone(), timezoneOffset));			
		}
	}

	
	public void setTimers(TimersRequest timersRequest) throws JsonProcessingException, InterruptedException, ApplicationException, DeviceOfflineException {
		var applicationException = new ApplicationException();

		for (int i=0; i<16; i++) {
			if (Boolean.TRUE.equals(timersRequest.getTimerModifiedArray()[i])) {
				var timer1Result = (TimerResult)executeCommand(timersRequest.getDeviceName(), CommandEnum.valueOf("TIMER"+(i+1)), objectMapper.writeValueAsString(timersRequest.getTimerArray()[i]));
				if (! /* not */ timer1Result.getTimerXX().equals(timersRequest.getTimerArray()[i])) {
					applicationException.addMessage(String.format(UNEXPECTED_RESULT_MESSAGE_FORMAT, "TIMER"+(i+1), timersRequest.getTimerArray()[i], timer1Result.getTimerXX(), timersRequest.getTimerArray()[i]));			
				}
			}
		}
		
		if (Boolean.TRUE.equals(timersRequest.getTimersModified())) {
			var timersResult = (TimersResult)executeCommand(timersRequest.getDeviceName(), CommandEnum.TIMERS,timersRequest.getTimers());
			if (! /* not */ StringUtils.equals(timersRequest.getTimers(), timersResult.getTimers())) {
				applicationException.addMessage(String.format(UNEXPECTED_RESULT_MESSAGE_FORMAT, CommandEnum.TIMERS, timersRequest, timersResult, timersRequest));
			}
		}
		
		if (! /* not */ CollectionUtils.isEmpty(applicationException.getMessageList())) {
			throw applicationException;
		}
	}
	
	public TimersResult getTimers(String device) throws InterruptedException, DeviceOfflineException {
		return (TimersResult)executeCommand(device, CommandEnum.TIMERS);

	}

	public void setTimeStdt(String deviceName, TimeStdRequest timeStdRequest) {
		// TODO Auto-generated method stub
		
	}
	
	
	private AbstractBaseResult executeCommand(String device, CommandEnum commandEnum) throws InterruptedException, DeviceOfflineException {
		return executeCommand(device, commandEnum, StringUtils.EMPTY, true);
		
	}
	public AbstractBaseResult executeCommand(String device, CommandEnum commandEnum, String stringArgument) throws InterruptedException, DeviceOfflineException {
		return executeCommand(device, commandEnum, stringArgument, true);
	}
	
	private AbstractBaseResult executeCommand(String deviceName, CommandEnum commandEnum, String stringArgument, boolean wait) throws InterruptedException, DeviceOfflineException {
		var device = deviceService.getDevice(deviceName);
		try {
			device.getLock().lock();
			if (wait) {
				var commandTimestamp = new Date().getTime();
				sendMessage(topicHelper.getCommandTopic(commandEnum, deviceName), stringArgument);
				return waitForCommandToExecute(deviceName, commandEnum, commandTimestamp); 
			} else {
				sendMessage(topicHelper.getCommandTopic(commandEnum, deviceName), stringArgument);
				return null;
			}
		} finally {
			device.getLock().unlock();
		}
	}

	private void sendMessage(String topic, String message) {
		LOGGER.info("Sending message [{}] to [{}] topic", message, topic);
		messageSender.sendMessage(topic, message);
	}

	private static final int SLEEP_MILLISECONDS = 100;
	private int maxNumberOfTries;
	@PostConstruct
	public void init () {
		maxNumberOfTries = commandExecutionTimeout * 1000 / SLEEP_MILLISECONDS;
	}
	public AbstractBaseResult waitForCommandToExecute(String deviceName, CommandEnum commandEnum, long commandTimestamp) throws InterruptedException, DeviceOfflineException {
    	AbstractBaseResult result;
    	int count = 0;
    	LOGGER.info("Waiting for command to finish execution ...");
		do {
			TimeUnit.MILLISECONDS.sleep(SLEEP_MILLISECONDS);
			count++;
			result = applicationCache.getCommandResult(deviceName, commandEnum);
		} while ((result == null && count < maxNumberOfTries) || (result != null && result.getTimestamp() <= commandTimestamp && count < maxNumberOfTries));
		LOGGER.info("Waited [{}] seconds", count * SLEEP_MILLISECONDS / 1000f);
		
		if (count == maxNumberOfTries) {
			LOGGER.warn("Timed out waiting for command [{}] to execute on device [{}]", commandEnum, deviceName);
			LOGGER.warn("Marking device [{}] as UNREACHABLE", deviceName);
			var stateMessage = new StateMessage(ConnectionStateEnum.UNREACHABLE, new Date().getTime());
			applicationCache.setConnectionState(deviceName, stateMessage);
			triggerPublishConnectionState(deviceName);
			throw new DeviceOfflineException();
		}
		return result;
    }


    public void triggerPublishConnectionState(String deviceName) {
    	LOGGER.info("Publishing StateMessage message [{}] of device [{}]", applicationCache.getConnectionState(deviceName), deviceName);
    	var webSocketTopic = websocketTopicsPrefix + "/state-and-telemetry/" + topicHelper.getStateTopic(deviceName);
    	webSocket.convertAndSend(webSocketTopic, applicationCache.getConnectionState(deviceName));
    }

}
