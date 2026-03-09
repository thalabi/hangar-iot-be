package com.kerneldc.hangariot.exception;

import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.kerneldc.hangariot.controller.HangarIotController;

@ControllerAdvice(basePackageClasses = HangarIotController.class)
public class HangarIotControllerExceptionHandler extends ResponseEntityExceptionHandler {


	@ExceptionHandler(InvalidDeviceException.class)
	protected ResponseEntity<String> handleInvalidDeviceException(InvalidDeviceException ex) {
		return new ResponseEntity<>(NestedExceptionUtils.getMostSpecificCause(ex).getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(UnexpectedCommandResultException.class)
	protected ResponseEntity<String> handleUnexpectedCommandResultException(UnexpectedCommandResultException ex) {
		return new ResponseEntity<>(NestedExceptionUtils.getMostSpecificCause(ex).getMessage(), HttpStatus.EXPECTATION_FAILED);
	}

	/**
	 * This exception return an OK Http status code and an informational message
	 * It is handled earlier before it reaches the controller by setting the device's connection state to UNREACHABLE
	 * 
	 * @param ex
	 * @return
	 */
	@ExceptionHandler(DeviceOfflineException.class)
	protected ResponseEntity<String> handleDeviceOfflineException(DeviceOfflineException ex) {
		return new ResponseEntity<>(NestedExceptionUtils.getMostSpecificCause(ex).getMessage(), HttpStatus.OK);
	}
	
}
