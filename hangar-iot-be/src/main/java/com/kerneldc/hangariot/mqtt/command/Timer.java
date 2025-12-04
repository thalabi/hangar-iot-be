package com.kerneldc.hangariot.mqtt.command;


import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class Timer {

	@NotNull(message = "Enable is missing")
	@JsonFormat(shape = Shape.NUMBER)
	private Boolean enable;
	@Min(value = 0, message = "Mode can only be 0, 1 or 2")
	@Max(value = 2, message = "Mode can only be 0, 1 or 2")
	private Integer mode;
	@NotBlank(message = "Time is missing")
	@Pattern(regexp = "\\d\\d:\\d\\d", message = "Specify time using format hh:mm")
	private String time;
	@NotNull(message = "Window is missing")
	private Integer window;
	@Length(min = 7, max = 7, message = "Days size should be seven")
	private String days;
	@NotNull(message = "Repeat is missing")
	@JsonFormat(shape = Shape.NUMBER)
	private Boolean repeat;
	@Min(value = 1, message = "Output should be a number > 0")
	private Integer output;
	@Min(value = 0, message = "Action can only be 0, 1, 2 or 3")
	@Max(value = 3, message = "Action can only be 0, 1, 2 or 3")
	private Integer action;

//	@JsonFormat(shape = Shape.NUMBER)
//	private Boolean enable;
//	private Integer mode;  // 0 use clock time, 1 use sunrise time and 2 use sunset time
//	private String time; // format hh:mm
//	private Integer window; // 0..15 add or subtract a random number of minutes to time
//	private String days; //SMTWTFS = set day of weeks mask where 0 or - = OFF and any different character = ON
//	@JsonFormat(shape = Shape.NUMBER)
//	private Boolean repeat; // 0 or 1
//	private Integer output; // 1..16 = select an output to be used if no rule is enabled
//	private Integer action; //0 = turn output OFF, 1 = turn output ON, 2 = TOGGLE output, 3 = RULE/BLINK
}
