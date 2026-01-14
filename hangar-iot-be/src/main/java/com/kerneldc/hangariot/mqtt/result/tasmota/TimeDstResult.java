package com.kerneldc.hangariot.mqtt.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString(callSuper = true)
public class TimeDstResult extends AbstractBaseResult {

	private TimeDst timeDst;
	
	@Getter @Setter
	@ToString
	public class TimeDst {
		private Integer hemisphere;
		private Integer week;
		private Integer month;
		private Integer day;
		private Integer hour;
		private Integer offset;
	}
}
