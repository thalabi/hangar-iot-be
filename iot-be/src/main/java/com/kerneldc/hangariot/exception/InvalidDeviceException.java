package com.kerneldc.hangariot.exception;

public class InvalidDeviceException extends ApplicationException {

	private static final long serialVersionUID = 1L;

	public InvalidDeviceException() {
		super();
	}

	public InvalidDeviceException(String message) {
		super(message);
	}

	public InvalidDeviceException(String message, Throwable arg1) {
		super(message, arg1);
	}

	public InvalidDeviceException(Throwable arg0) {
		super(arg0);
	}

	public InvalidDeviceException(String message, Throwable arg1, boolean arg2, boolean arg3) {
		super(message, arg1, arg2, arg3);
	}

}
