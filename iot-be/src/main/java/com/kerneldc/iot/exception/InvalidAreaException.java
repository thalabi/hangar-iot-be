package com.kerneldc.iot.exception;

public class InvalidAreaException extends ApplicationException {

	private static final long serialVersionUID = 1L;

	public InvalidAreaException() {
		super();
	}

	public InvalidAreaException(String message) {
		super(message);
	}

	public InvalidAreaException(String message, Throwable arg1) {
		super(message, arg1);
	}

	public InvalidAreaException(Throwable arg0) {
		super(arg0);
	}

	public InvalidAreaException(String message, Throwable arg1, boolean arg2, boolean arg3) {
		super(message, arg1, arg2, arg3);
	}

}
