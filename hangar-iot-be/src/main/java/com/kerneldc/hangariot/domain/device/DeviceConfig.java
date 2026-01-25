package com.kerneldc.hangariot.domain.device;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class DeviceConfig implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Float latitudeDegrees;
	private Float longitudeDegrees;
	private String timezoneOffset;
	private String timeDst;
	private String timeStd;
}