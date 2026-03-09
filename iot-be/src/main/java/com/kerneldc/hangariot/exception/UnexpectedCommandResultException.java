package com.kerneldc.hangariot.exception;

public class UnexpectedCommandResultException extends ApplicationException {

	private static final long serialVersionUID = 1L;
	public UnexpectedCommandResultException() {
		super();
	}

	public UnexpectedCommandResultException(String message, Throwable arg1, boolean arg2, boolean arg3) {
		super(message, arg1, arg2, arg3);
	}

	public UnexpectedCommandResultException(String message, Throwable arg1) {
		super(message, arg1);
	}

	public UnexpectedCommandResultException(String message) {
		super(message);
	}

	public UnexpectedCommandResultException(Throwable arg0) {
		super(arg0);
	}

}
