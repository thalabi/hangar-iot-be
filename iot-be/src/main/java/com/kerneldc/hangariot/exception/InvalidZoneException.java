package com.kerneldc.hangariot.exception;

public class InvalidZoneException extends ApplicationException {

	private static final long serialVersionUID = 1L;

	public InvalidZoneException() {
		super();
	}

	public InvalidZoneException(String message) {
		super(message);
	}

	public InvalidZoneException(String message, Throwable arg1) {
		super(message, arg1);
	}

	public InvalidZoneException(Throwable arg0) {
		super(arg0);
	}

	public InvalidZoneException(String message, Throwable arg1, boolean arg2, boolean arg3) {
		super(message, arg1, arg2, arg3);
	}

}
