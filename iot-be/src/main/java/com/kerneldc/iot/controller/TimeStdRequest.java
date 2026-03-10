package com.kerneldc.iot.controller;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TimeStdRequest {

	@NotBlank(message = "Device name is missing")
	private String deviceName;
	@NotNull(message = "Hemisphere is missing")
	@Size(min = 0, max = 1, message = "hemisphere cannot be zero or one")
	private Integer hemisphere;
	@NotNull(message = "Week is missing")
	@Size(min = 1, max = 52, message = "Week must be between one and 52")
	private Integer week;
	@NotNull(message = "Month is missing")
	@Size(min = 1, max = 12, message = "Month must be between one and 12")
	private Integer month;
	@NotNull(message = "Day is missing")
	@Size(min = 1, max = 31, message = "Day must be between one and 31")
	private Integer day;
	@NotNull(message = "Hour is missing")
	@Size(min = 1, max = 24, message = "Hour must be between one and 24")
	private Integer hour;
	@NotNull(message = "Offset is missing")
	private Integer offset;

}
