package com.kerneldc.hangariot.mqtt.result;

import com.kerneldc.hangariot.mqtt.result.timer.TimerResult;
import com.kerneldc.hangariot.mqtt.result.timer.TimersResult;

public enum CommandEnum {
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
	BACKLOG("backlog", null);
	
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
