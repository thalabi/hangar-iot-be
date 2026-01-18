package com.kerneldc.hangariot.mqtt.result.tasmota;

import com.kerneldc.hangariot.mqtt.result.AbstractBaseResult;
import com.kerneldc.hangariot.mqtt.result.tasmota.timer.TimerResult;
import com.kerneldc.hangariot.mqtt.result.tasmota.timer.TimersResult;
import com.kerneldc.hangariot.mqtt.result.zigbee2mqtt.StateResult;

public enum CommandEnum {
	/*
	 * Tasmota commands
	 */
	POWER("power", PowerResult.class),
	TIMEZONE("timezone", TimezoneResult.class),
	TIMEDST("timedst", TimeDstResult.class),
	TIMESTD("timestd", TimeStdResult.class),
	TIME("time", TimeResult.class),
	TELEPERIOD("teleperiod", TelePeriodResult.class),
	LATITUDE("latitude", LatitudeResult.class),
	LONGITUDE("longitude", LongitudeResult.class),
	
	TIMERS("timers", TimersResult.class),
	TIMER1("timer1", TimerResult.class),
	TIMER2("timer2", TimerResult.class),
	TIMER3("timer3", TimerResult.class),
	TIMER4("timer4", TimerResult.class),
	TIMER5("timer5", TimerResult.class),
	TIMER6("timer6", TimerResult.class),
	TIMER7("timer7", TimerResult.class),
	TIMER8("timer8", TimerResult.class),
	TIMER9("timer9", TimerResult.class),
	TIMER10("timer10", TimerResult.class),
	TIMER11("timer11", TimerResult.class),
	TIMER12("timer12", TimerResult.class),
	TIMER13("timer13", TimerResult.class),
	TIMER14("timer14", TimerResult.class),
	TIMER15("timer15", TimerResult.class),
	TIMER16("timer16", TimerResult.class),
	BACKLOG("backlog", null),
	/*
	 * Zigbee2mqtt command
	 */
	ZIGBEE2MQTT_STATE("state", StateResult.class),
	;
	
	String command;
	Class<? extends AbstractBaseResult> resultType;
	
	CommandEnum(String command, Class<? extends AbstractBaseResult> resultType) {
		this.command = command;
		this.resultType = resultType;
	}

	public String getCommand() {
		return command;
	}

	public Class<? extends AbstractBaseResult> getResultType() {
		return resultType;
	}

}
