package com.kerneldc.hangariot.util;

import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class TimeUtils {
	
	public static LocalTime epochMilliToLocalTime(long epochMilli) {
		return Instant.ofEpochMilli(epochMilli)
                .atZone(ZoneId.systemDefault())
                .toLocalTime();
	}

	public static OffsetDateTime epochMilliToOffsetDateTime(long epochMilli) {
		Instant instant = Instant.ofEpochMilli(epochMilli);
		return OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
	}

}
