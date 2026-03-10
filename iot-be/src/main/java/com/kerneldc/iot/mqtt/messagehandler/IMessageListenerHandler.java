package com.kerneldc.iot.mqtt.messagehandler;

public interface IMessageListenerHandler {

	boolean canHandleMessage(String fullTopic);
	void handleMessage(String fullTopic, long timestamp, String message);
}
