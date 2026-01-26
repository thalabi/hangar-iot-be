package com.kerneldc.hangariot.mqtt.messagehandler;

public interface IMessageListenerHandler {

	boolean canHandleMessage(String fullTopic);
	void handleMessage(String fullTopic, long timestamp, String message);
}
