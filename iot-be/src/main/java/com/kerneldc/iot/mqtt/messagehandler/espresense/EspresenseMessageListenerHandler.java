package com.kerneldc.iot.mqtt.messagehandler.espresense;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.iot.domain.device.Device;
import com.kerneldc.iot.mqtt.messagehandler.AbstractMessageListenerHandler;
import com.kerneldc.iot.mqtt.service.ApplicationContext;
import com.kerneldc.iot.mqtt.topic.TopicHelper;
import com.kerneldc.iot.util.TimeUtils;
import com.kerneldc.iot.websocket.ConnectionStateEnum;
import com.kerneldc.iot.websocket.message.ConnectionStateMessage;
import com.kerneldc.iot.websocket.service.WebSocketSenderService;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EspresenseMessageListenerHandler extends AbstractMessageListenerHandler{

	// One single scheduler shared by ALL devices
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    // Map to keep track of the active watchdog task for each device
    private final Map<Device, ScheduledFuture<?>> activeTimers = new ConcurrentHashMap<>();
    
    
	public EspresenseMessageListenerHandler(ApplicationContext applicationContext, ObjectMapper objectMapper,
			WebSocketSenderService webSocketSenderService, TopicHelper topicHelper) {
		super(applicationContext, objectMapper, webSocketSenderService, topicHelper);
	}

	public void handleMessage(String fullTopic, long timestamp, String message) {

		LOGGER.info("Begin EspresenseMessageListenerHandler ...");
		
		var device = topicHelper.getDeviceFromTopic(fullTopic);

		
		LOGGER.info("fullTopic [{}], timestamp [{}], message [{}]", fullTopic, TimeUtils.epochMilliToLocalTime(timestamp), message);
		try {
			message = addTimeStampToMessage(timestamp, message);

			// state message
			webSocketSenderService.publishEspresenseState(fullTopic, message);
						
			// publish attribute changes
//			webSocketSenderService.publishEspresenseAttributeChanges(device);
			
		} catch (JsonProcessingException e) {
			throw new MessagingException("Failed to add message to cache.", e);
		}

		
		LOGGER.info("activeTimers size [{}]", activeTimers.size());
		// Cancel the existing timer for this specific device
        ScheduledFuture<?> existingTimer = activeTimers.remove(device);
        if (existingTimer != null) {
        	LOGGER.info("Cancelling timer for device [{}]", device.getName());
            existingTimer.cancel(false);
        } else {
            // If there was no timer, the device was either offline or new
            setOnlineAndPublish(device);
        }
		
        // Schedule a new offline task 
        ScheduledFuture<?> newTimer = scheduler.schedule(
                () -> setOfflineAndPublish(device), 
                120, // two minutes
                TimeUnit.SECONDS
            );
        activeTimers.put(device, newTimer);
        
		LOGGER.info("End EspresenseMessageListenerHandler ...");
	}
	
	private void setOnlineAndPublish(Device device) {
		applicationContext.setConnectionState(device, new ConnectionStateMessage(ConnectionStateEnum.ONLINE, System.currentTimeMillis()));
		webSocketSenderService.publishConnectionState(device);
		
	}

	private void setOfflineAndPublish(Device device) {
		activeTimers.remove(device);
		applicationContext.setConnectionState(device, new ConnectionStateMessage(ConnectionStateEnum.OFFLINE, System.currentTimeMillis()));
		webSocketSenderService.publishConnectionState(device);
		
	}

	@PreDestroy
    public void shutdown() {
		scheduler.shutdownNow();
	}
	
	@Override
	public boolean canHandleMessage(String fullTopic) {
		return topicHelper.isEspresenseDeviceTopic(fullTopic);
	}
}

