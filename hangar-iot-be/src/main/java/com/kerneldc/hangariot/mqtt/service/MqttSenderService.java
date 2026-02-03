package com.kerneldc.hangariot.mqtt.service;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.hangariot.controller.TimeStdRequest;
import com.kerneldc.hangariot.controller.TimersRequest;
import com.kerneldc.hangariot.domain.device.Device;
import com.kerneldc.hangariot.domain.enums.BridgeEnum;
import com.kerneldc.hangariot.exception.ApplicationException;
import com.kerneldc.hangariot.exception.ApplicationRuntimeException;
import com.kerneldc.hangariot.exception.DeviceOfflineException;
import com.kerneldc.hangariot.exception.UnexpectedCommandResultException;
import com.kerneldc.hangariot.mqtt.command.ICommandEnum;
import com.kerneldc.hangariot.mqtt.command.TasmotaCommandEnum;
import com.kerneldc.hangariot.mqtt.command.Zigbee2MqttCommandEnum;
import com.kerneldc.hangariot.mqtt.result.AbstractBaseResult;
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

	public void togglePower(Device device, String powerStateExpected) throws ApplicationException, DeviceOfflineException {
		if (device.getBridge() == BridgeEnum.ZIGBEE2MQTT) {
			var result = (StateResult)sendMessage(device, Zigbee2MqttCommandEnum.TOGGLE_POWER);
			if (! /* not */ StringUtils.equalsIgnoreCase(powerStateExpected, result.getState())) {
				throw new ApplicationException(String.format(UNEXPECTED_RESULT_MESSAGE_FORMAT, Zigbee2MqttCommandEnum.TOGGLE_POWER, StringUtils.EMPTY, result.getState(), powerStateExpected));
			}
		} else {
			var result = (PowerResult)sendMessage(device, TasmotaCommandEnum.POWER, "2"); // 2 toggles power
			if (! /* not */ StringUtils.equalsIgnoreCase(powerStateExpected, result.getPower())) {
				throw new ApplicationException(String.format(UNEXPECTED_RESULT_MESSAGE_FORMAT, TasmotaCommandEnum.POWER, "2", result.getPower(), powerStateExpected));
			}
		}
	}

	
	/**
	 * Handles only Zigbee2mqtt devices
	 * 
	 * @param device
	 */
	public void triggerPublishConnectionState(Device device) {
		LOGGER.info("triggerPublishConnectionState(\"{}\") begin", device.getName());
		try {
			sendMessage(device, Zigbee2MqttCommandEnum.GET_STATE);
		} catch (DeviceOfflineException e) {
			LOGGER.warn("Exception [{}] thrown. Device state will be set as UNREACHABLE.", e.getClass().getSimpleName());
		}
		LOGGER.info("triggerPublishConnectionState(\"{}\") end", device.getName());
	}

	public void triggerPublishPowerState(Device device) throws DeviceOfflineException {
		LOGGER.info("triggerPublishPowerState(\"{}\") begin", device.getName());
		if (device.getBridge() == BridgeEnum.ZIGBEE2MQTT) {
			sendMessage(device, Zigbee2MqttCommandEnum.GET_STATE, STATE_PAYLOAD);
		} else {
			sendMessage(device, TasmotaCommandEnum.POWER);
		}
		LOGGER.info("triggerPublishPowerState(\"{}\") end", device.getName());
	}

	public void triggerPublishSensorData(Device device) throws ApplicationException {
		checkDeviceOnline(device);
		// issue the command without an argument to get the teleperiod value
		var result = (TelePeriodResult)sendMessage(device, TasmotaCommandEnum.TELEPERIOD);
		// issue the command again with the retrieved argument to trigger an update on the SENSOR topic
		var result2 = (TelePeriodResult)sendMessage(device, TasmotaCommandEnum.TELEPERIOD, String.valueOf(result.getTelePeriod()));
		if (! /* not */ result.getTelePeriod().equals(result2.getTelePeriod())) {
			throw new UnexpectedCommandResultException(String.format(UNEXPECTED_RESULT_MESSAGE_FORMAT, TasmotaCommandEnum.TELEPERIOD, result.getTelePeriod(), result2.getTelePeriod(), result.getTelePeriod()));
		}
	}

	public void triggerTimezoneValue(Device device) throws DeviceOfflineException {
		sendMessage(device, TasmotaCommandEnum.TIMEZONE);
	}

	private void checkDeviceOnline(Device device) throws DeviceOfflineException {
		if (! /* not */ applicationContext.isDeviceOnLine(device)) {
			throw new DeviceOfflineException();
		}
	}

	public void setTelePeriod(Device device, String telePeriod) throws ApplicationException, DeviceOfflineException {
		var result = (TelePeriodResult)sendMessage(device, TasmotaCommandEnum.TELEPERIOD, telePeriod);
		if (! /* not */ applicationContext.isDeviceOnLine(device)) {			
			return;
		}
		if (! /* not */ result.getTelePeriod().equals(Integer.valueOf(telePeriod))) {
			throw new ApplicationException(String.format(UNEXPECTED_RESULT_MESSAGE_FORMAT, TasmotaCommandEnum.TELEPERIOD, telePeriod, result.getTelePeriod(), telePeriod));			
		}
	}

	public void setTimezoneOffset(Device device, String timezoneOffset) throws ApplicationException, DeviceOfflineException {
		var result = (TimezoneResult)sendMessage(device, TasmotaCommandEnum.TIMEZONE, timezoneOffset);
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
			throw new ApplicationException(String.format(UNEXPECTED_RESULT_MESSAGE_FORMAT, TasmotaCommandEnum.TIMEZONE, timezoneOffset, result.getTimezone(), timezoneOffset));			
		}
	}

	
	public void setTimers(TimersRequest timersRequest) throws JsonProcessingException, ApplicationException, DeviceOfflineException {
		var applicationException = new ApplicationException();
		var device = deviceService.getDevice(timersRequest.getDeviceName());

		for (int i=0; i<16; i++) {
			if (Boolean.TRUE.equals(timersRequest.getTimerModifiedArray()[i])) {
				var timer1Result = (TimerResult)sendMessage(device, TasmotaCommandEnum.valueOf("TIMER"+(i+1)), objectMapper.writeValueAsString(timersRequest.getTimerArray()[i]));
				if (! /* not */ timer1Result.getTimerXX().equals(timersRequest.getTimerArray()[i])) {
					applicationException.addMessage(String.format(UNEXPECTED_RESULT_MESSAGE_FORMAT, "TIMER"+(i+1), timersRequest.getTimerArray()[i], timer1Result.getTimerXX(), timersRequest.getTimerArray()[i]));			
				}
			}
		}
		
		if (Boolean.TRUE.equals(timersRequest.getTimersModified())) {
			var timersResult = (TimersResult)sendMessage(device, TasmotaCommandEnum.TIMERS,timersRequest.getTimers());
			if (! /* not */ StringUtils.equals(timersRequest.getTimers(), timersResult.getTimers())) {
				applicationException.addMessage(String.format(UNEXPECTED_RESULT_MESSAGE_FORMAT, TasmotaCommandEnum.TIMERS, timersRequest, timersResult, timersRequest));
			}
		}
		
		if (! /* not */ CollectionUtils.isEmpty(applicationException.getMessageList())) {
			throw applicationException;
		}
	}
	
	public TimersResult getTimers(Device device) throws DeviceOfflineException {
		return (TimersResult)sendMessage(device, TasmotaCommandEnum.TIMERS);

	}

	public void setTimeStdt(String deviceName, TimeStdRequest timeStdRequest) {
		// TODO Auto-generated method stub
		
	}
	
	
	private AbstractBaseResult sendMessage(Device device, ICommandEnum commandEnum) throws DeviceOfflineException {
		switch (commandEnum.handlesBridge()) {
		case ZIGBEE2MQTT: {
			return sendMessage(device, commandEnum, ((Zigbee2MqttCommandEnum)commandEnum).getPayload(), true);
		}
		case TASMOTA: {
			return sendMessage(device, commandEnum, StringUtils.EMPTY, true);
		}
		}
		return null;
		
	}
	public AbstractBaseResult sendMessage(Device device, ICommandEnum commandEnum, String message) throws DeviceOfflineException {
		return sendMessage(device, commandEnum, message, true);
	}
	
	private AbstractBaseResult sendMessage(Device device, ICommandEnum commandEnum, String message, boolean wait) throws DeviceOfflineException {
		LOGGER.info("device name [{}], commandEnum [{}], message [{}], wait [{}]", device.getName(), commandEnum, message, wait);
		var topic = topicHelper.getCommandTopic(commandEnum, device);
		LOGGER.info("Sending mqtt message [{}] to topic [{}]", message, topic);
		var lock = device.getLock();
		
		try {
			if (!lock.tryLock(5, TimeUnit.SECONDS)) {
			    throw new ApplicationRuntimeException("Could not acquire device lock after 5 seconds- system busy");
			}
		} catch (InterruptedException _) {
			markDeviceUnreachable(device);
			Thread.currentThread().interrupt();
			throw new DeviceOfflineException();
		}
		
		try {
			var commandTimestamp = System.currentTimeMillis();
	        mqqtGateway.sendMessage(topic, message);
	        
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
	private AbstractBaseResult waitForMessageSendToComplete(Device device, ICommandEnum iCommandEnum, long commandIssuedTimestamp) throws DeviceOfflineException {
    	AbstractBaseResult result;
    	int count = 0;
    	LOGGER.info("Waiting for message send to complete ...");
		do {
			try {
				TimeUnit.MILLISECONDS.sleep(SLEEP_MILLISECONDS);
			} catch (InterruptedException _) {
				markDeviceUnreachable(device);
				Thread.currentThread().interrupt();
				throw new DeviceOfflineException();
			}
			count++;
			result = applicationContext.getCommandResult(device, iCommandEnum);
			LOGGER.info("result [{}] count [{}] maxNumberOfTries [{}] result.getTimestamp() [{}] commandIssuedTimestamp [{}]", result, count, maxNumberOfTries, (result != null ? result.getTimestamp() : ""), commandIssuedTimestamp);
		} while ((result == null && count < maxNumberOfTries) || (result != null && result.getTimestamp() <= commandIssuedTimestamp && count < maxNumberOfTries));
		LOGGER.info("Waited [{}] seconds", count * SLEEP_MILLISECONDS / 1000f);
		
		if (count == maxNumberOfTries) {
			LOGGER.warn("Timed out waiting for command [{}] to execute on device [{}]", iCommandEnum, device.getName());
			markDeviceUnreachable(device);
			throw new DeviceOfflineException();
		}
		return result;
    }
	private void markDeviceUnreachable(Device device) {
		LOGGER.warn("Marking device [{}] as UNREACHABLE", device.getName());
		var stateMessage = new ConnectionStateMessage(ConnectionStateEnum.UNREACHABLE, System.currentTimeMillis());
		applicationContext.setConnectionState(device, stateMessage);
		webSocketSenderService.publishConnectionState(device);
	}

}
