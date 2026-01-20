package com.kerneldc.hangariot.mqtt.service;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.hangariot.controller.Device;
import com.kerneldc.hangariot.controller.Device.BridgeEnum;
import com.kerneldc.hangariot.controller.TimeStdRequest;
import com.kerneldc.hangariot.controller.TimersRequest;
import com.kerneldc.hangariot.exception.ApplicationException;
import com.kerneldc.hangariot.exception.ApplicationRuntimeException;
import com.kerneldc.hangariot.exception.DeviceOfflineException;
import com.kerneldc.hangariot.exception.UnexpectedCommandResultException;
import com.kerneldc.hangariot.mqtt.result.AbstractBaseResult;
import com.kerneldc.hangariot.mqtt.result.tasmota.CommandEnum;
import com.kerneldc.hangariot.mqtt.result.tasmota.PowerResult;
import com.kerneldc.hangariot.mqtt.result.tasmota.TelePeriodResult;
import com.kerneldc.hangariot.mqtt.result.tasmota.TimezoneResult;
import com.kerneldc.hangariot.mqtt.result.tasmota.timer.TimerResult;
import com.kerneldc.hangariot.mqtt.result.tasmota.timer.TimersResult;
import com.kerneldc.hangariot.mqtt.result.zigbee2mqtt.StateResult;
import com.kerneldc.hangariot.mqtt.topic.TopicHelper;
import com.kerneldc.hangariot.springconfig.MqttConfig.MqqtGateway;
import com.kerneldc.hangariot.websocket.ConnectionStateEnum;
import com.kerneldc.hangariot.websocket.message.ConnectionStateMessage;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttSenderService {

	// MQTT messages
	private final MqqtGateway mqqtGateway;
	// WebSocket messages
	private final TopicHelper topicHelper;
	private final ApplicationContext applicationContext;
	private final ObjectMapper objectMapper;
	private final DeviceService deviceService;
	private final WebSocketSenderService webSocketSenderService;
	
	@Value("${command.execution.timeout:5}")
	private Integer commandExecutionTimeout;

	@Value("${websocket.topics.prefix:/topic}")
	private String websocketTopicsPrefix;

	private static final String UNEXPECTED_RESULT_MESSAGE_FORMAT = "Executing [%s] command with argument [%s] failed. Result came back as [%s], expected [%s]";
	private static final String STATE_PAYLOAD = """
			{"state": ""}
			""";
	private static final String STATE_TOGGLE_PAYLOAD = """
			{"state": "toggle"}
			""";

	public void togglePower(Device device, String powerStateExpected) throws InterruptedException, ApplicationException, DeviceOfflineException {
		if (device.getBridge() == BridgeEnum.ZIGBEE2MQTT) {
			var result = (StateResult)sendMessage(device, CommandEnum.ZIGBEE2MQTT_STATE, STATE_TOGGLE_PAYLOAD);
			if (! /* not */ StringUtils.equalsIgnoreCase(powerStateExpected, result.getState())) {
				throw new ApplicationException(String.format(UNEXPECTED_RESULT_MESSAGE_FORMAT, CommandEnum.ZIGBEE2MQTT_STATE, STATE_TOGGLE_PAYLOAD, result.getState(), powerStateExpected));
			}
		} else {
			var result = (PowerResult)sendMessage(device, CommandEnum.POWER, "2"); // 2 toggles power
			if (! /* not */ StringUtils.equalsIgnoreCase(powerStateExpected, result.getPower())) {
				throw new ApplicationException(String.format(UNEXPECTED_RESULT_MESSAGE_FORMAT, CommandEnum.POWER, "2", result.getPower(), powerStateExpected));
			}
		}
	}

	public void triggerPublishConnectionState(Device device) {
		LOGGER.info("triggerPublishConnectionState(\"{}\") begin", device.getName());
		try {
			sendMessage(device, CommandEnum.ZIGBEE2MQTT_STATE, STATE_PAYLOAD);
		} catch (InterruptedException _) {
			Thread.currentThread().interrupt();
		} catch (DeviceOfflineException e) {
			LOGGER.warn("Exception [{}] thrown. Device state will be set as UNREACHABLE.", e.getClass().getSimpleName());
		}
		LOGGER.info("triggerPublishConnectionState(\"{}\") end", device.getName());
	}

	public void triggerPublishPowerState(Device device) throws InterruptedException, DeviceOfflineException {
		LOGGER.info("triggerPublishPowerState(\"{}\") begin", device.getName());
		if (device.getBridge() == BridgeEnum.ZIGBEE2MQTT) {
			sendMessage(device, CommandEnum.ZIGBEE2MQTT_STATE,STATE_PAYLOAD);
		} else {
			sendMessage(device, CommandEnum.POWER);
		}
		LOGGER.info("triggerPublishPowerState(\"{}\") end", device.getName());
	}

	public void triggerPublishSensorData(Device device) throws InterruptedException, ApplicationException {
		checkDeviceOnline(device);
		// issue the command without an argument to get the teleperiod value
		var result = (TelePeriodResult)sendMessage(device, CommandEnum.TELEPERIOD);
		// issue the command again with the retrieved argument to trigger an update on the SENSOR topic
		var result2 = (TelePeriodResult)sendMessage(device, CommandEnum.TELEPERIOD, String.valueOf(result.getTelePeriod()));
		if (! /* not */ result.getTelePeriod().equals(result2.getTelePeriod())) {
			throw new UnexpectedCommandResultException(String.format(UNEXPECTED_RESULT_MESSAGE_FORMAT, CommandEnum.TELEPERIOD, result.getTelePeriod(), result2.getTelePeriod(), result.getTelePeriod()));
		}
	}

	public void triggerTimezoneValue(Device device) throws InterruptedException, DeviceOfflineException {
		sendMessage(device, CommandEnum.TIMEZONE);
	}

	private void checkDeviceOnline(Device device) throws DeviceOfflineException {
		if (! /* not */ applicationContext.isDeviceOnLine(device)) {
			throw new DeviceOfflineException();
		}
	}

	public void setTelePeriod(Device device, String telePeriod) throws InterruptedException, ApplicationException, DeviceOfflineException {
		var result = (TelePeriodResult)sendMessage(device, CommandEnum.TELEPERIOD, telePeriod);
		if (! /* not */ applicationContext.isDeviceOnLine(device)) {			
			return;
		}
		if (! /* not */ result.getTelePeriod().equals(Integer.valueOf(telePeriod))) {
			throw new ApplicationException(String.format(UNEXPECTED_RESULT_MESSAGE_FORMAT, CommandEnum.TELEPERIOD, telePeriod, result.getTelePeriod(), telePeriod));			
		}
	}

	public void setTimezoneOffset(Device device, String timezoneOffset) throws InterruptedException, ApplicationException, DeviceOfflineException {
		var result = (TimezoneResult)sendMessage(device, CommandEnum.TIMEZONE, timezoneOffset);
		if (! /* not */ applicationContext.isDeviceOnLine(device)) {
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
		var device = deviceService.getDevice(timersRequest.getDeviceName());

		for (int i=0; i<16; i++) {
			if (Boolean.TRUE.equals(timersRequest.getTimerModifiedArray()[i])) {
				var timer1Result = (TimerResult)sendMessage(device, CommandEnum.valueOf("TIMER"+(i+1)), objectMapper.writeValueAsString(timersRequest.getTimerArray()[i]));
				if (! /* not */ timer1Result.getTimerXX().equals(timersRequest.getTimerArray()[i])) {
					applicationException.addMessage(String.format(UNEXPECTED_RESULT_MESSAGE_FORMAT, "TIMER"+(i+1), timersRequest.getTimerArray()[i], timer1Result.getTimerXX(), timersRequest.getTimerArray()[i]));			
				}
			}
		}
		
		if (Boolean.TRUE.equals(timersRequest.getTimersModified())) {
			var timersResult = (TimersResult)sendMessage(device, CommandEnum.TIMERS,timersRequest.getTimers());
			if (! /* not */ StringUtils.equals(timersRequest.getTimers(), timersResult.getTimers())) {
				applicationException.addMessage(String.format(UNEXPECTED_RESULT_MESSAGE_FORMAT, CommandEnum.TIMERS, timersRequest, timersResult, timersRequest));
			}
		}
		
		if (! /* not */ CollectionUtils.isEmpty(applicationException.getMessageList())) {
			throw applicationException;
		}
	}
	
	public TimersResult getTimers(Device device) throws InterruptedException, DeviceOfflineException {
		return (TimersResult)sendMessage(device, CommandEnum.TIMERS);

	}

	public void setTimeStdt(String deviceName, TimeStdRequest timeStdRequest) {
		// TODO Auto-generated method stub
		
	}
	
	
	private AbstractBaseResult sendMessage(Device device, CommandEnum commandEnum) throws InterruptedException, DeviceOfflineException {
		return sendMessage(device, commandEnum, StringUtils.EMPTY, true);
		
	}
	public AbstractBaseResult sendMessage(Device device, CommandEnum commandEnum, String stringArgument) throws InterruptedException, DeviceOfflineException {
		return sendMessage(device, commandEnum, stringArgument, true);
	}
	
	private AbstractBaseResult sendMessage(Device device, CommandEnum commandEnum, String stringArgument, boolean wait) throws InterruptedException, DeviceOfflineException {
		var topic = topicHelper.getCommandTopic(commandEnum, device);
		LOGGER.info("Sending mqtt message [{}] to topic [{}]", stringArgument, topic);
		var lock = device.getLock();
		
		if (!lock.tryLock(5, TimeUnit.SECONDS)) {
	        throw new ApplicationRuntimeException("Could not acquire device lock after 5 seconds- system busy");
	    }
		
		try {
			var commandTimestamp = System.currentTimeMillis();
	        mqqtGateway.sendMessage(topic, stringArgument);
	        
			if (wait) {
				return waitForMessageSendToComplete(device, commandEnum, commandTimestamp); 
			} else {
				return null;
			}
		} finally {
			lock.unlock();
		}
	}

	private static final int SLEEP_MILLISECONDS = 100;
	private int maxNumberOfTries;
	@PostConstruct
	public void init () {
		maxNumberOfTries = commandExecutionTimeout * 1000 / SLEEP_MILLISECONDS;
	}
	private AbstractBaseResult waitForMessageSendToComplete(Device device, CommandEnum commandEnum, long commandIssuedTimestamp) throws InterruptedException, DeviceOfflineException {
    	AbstractBaseResult result;
    	int count = 0;
    	LOGGER.info("Waiting for message send to complete ...");
		do {
			TimeUnit.MILLISECONDS.sleep(SLEEP_MILLISECONDS);
			count++;
			result = applicationContext.getCommandResult(device, commandEnum);
			LOGGER.info("result [{}] count [{}] maxNumberOfTries [{}] result.getTimestamp() [{}] commandIssuedTimestamp [{}]", result, count, maxNumberOfTries, (result != null ? result.getTimestamp() : ""), commandIssuedTimestamp);
		} while ((result == null && count < maxNumberOfTries) || (result != null && result.getTimestamp() <= commandIssuedTimestamp && count < maxNumberOfTries));
		LOGGER.info("Waited [{}] seconds", count * SLEEP_MILLISECONDS / 1000f);
		
		if (count == maxNumberOfTries) {
			LOGGER.warn("Timed out waiting for command [{}] to execute on device [{}]", commandEnum, device.getName());
			LOGGER.warn("Marking device [{}] as UNREACHABLE", device.getName());
			var stateMessage = new ConnectionStateMessage(ConnectionStateEnum.UNREACHABLE, System.currentTimeMillis());
			applicationContext.setConnectionState(device, stateMessage);
			webSocketSenderService.publishConnectionState(device);
			throw new DeviceOfflineException();
		}
		return result;
    }

}
