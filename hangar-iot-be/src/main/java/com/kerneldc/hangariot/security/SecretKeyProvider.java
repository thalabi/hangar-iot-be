package com.kerneldc.hangariot.security;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
@Component
@Slf4j
public class SecretKeyProvider {
	@Value("${application.security.enableTestSecretKey:false}")
	private boolean enableTestSecretKey;

	@Getter
	private Key authenticationJwtKey;
	
	@PostConstruct
	private void init() throws NoSuchAlgorithmException {
		if (enableTestSecretKey) {
			LOGGER.warn("*** test secret key used to generate JWT token ***");
			LOGGER.warn("*** to disable remove application.security.enableTestSecretKey property or set to false ***");
			byte[] keyByteArray = {1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1,2};
			authenticationJwtKey = new SecretKeySpec(keyByteArray, "HmacSHA256");
			LOGGER.debug("authenticationJwtKey: {}", authenticationJwtKey.hashCode());
		} else {
			authenticationJwtKey = KeyGenerator.getInstance("HmacSHA256").generateKey();
		}
	}
	
	public Key generatePasswordResetJwtKey() throws NoSuchAlgorithmException {
		return KeyGenerator.getInstance("HmacSHA256").generateKey();
	}
}
