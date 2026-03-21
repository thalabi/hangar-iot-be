package com.kerneldc.iot.util;

import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class TimeUtils {
	
	 private TimeUtils() {
	  /* This utility class should not be instantiated */
	 }

	public static LocalTime epochMilliToLocalTime(long epochMilli) {
		return Instant.ofEpochMilli(epochMilli)
                .atZone(ZoneId.systemDefault())
                .toLocalTime();
	}

	public static OffsetDateTime epochMilliToOffsetDateTime(long epochMilli) {
		Instant instant = Instant.ofEpochMilli(epochMilli);
		return OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
	}
	
	public static boolean isSameMinute(long epochMilli1, long epochMilli2) {
		 return epochMilli1 / 60000L == epochMilli2 / 60000L;
	}

}
