package com.kerneldc.hangariot.exception;

public class DeviceOfflineException extends ApplicationException {

	private static final long serialVersionUID = 1L;
	public DeviceOfflineException() {
		super();
	}

	public DeviceOfflineException(String message, Throwable arg1, boolean arg2, boolean arg3) {
		super(message, arg1, arg2, arg3);
	}

	public DeviceOfflineException(String message, Throwable arg1) {
		super(message, arg1);
	}

	public DeviceOfflineException(String message) {
		super(message);
	}

	public DeviceOfflineException(Throwable arg0) {
		super(arg0);
	}

}
