package com.kerneldc.hangariot.security.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.kerneldc.hangariot.security.CustomUserDetails;
import com.kerneldc.hangariot.security.JwtUtil;

import jakarta.validation.Valid;

//@RestController
//@RequestMapping("securityController")
//@RequiredArgsConstructor
//@Slf4j
public class SecurityController {

    private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
//	private final SecurityService securityService;

    @PostMapping("/authenticate")
	public ResponseEntity<LoginResponse> auhenticate(@Valid @RequestBody LoginRequest loginRequest) {
		LOGGER.debug("Begin ...");
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
		try {
			Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
			
			LOGGER.debug("authentication: {}", authentication);
			
			CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
			customUserDetails.setPassword("********");
			
			String token = jwtUtil.generateToken(customUserDetails);
			
			LoginResponse loginResponse = new LoginResponse(customUserDetails, token);
			
			LOGGER.debug("End ...");
			return ResponseEntity.ok(loginResponse);

		} catch (AuthenticationException ex) {
			LOGGER.error(ex.getMessage());
			LOGGER.debug("End ...");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
	}

//    @PostMapping("/forgotPassword")
//	public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
//		LOGGER.debug("Begin ...");
//		LOGGER.debug("forgotPasswordRequest: {}", forgotPasswordRequest);
//		try {
//			securityService.processForgotPasswordRequest(forgotPasswordRequest.getEmail(), forgotPasswordRequest.getBaseUrl());
//		} catch (EntityNotFoundException e) {
//			LOGGER.warn(e.getMessage());
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//		} catch (NoSuchAlgorithmException | MessagingException | IOException | TemplateException e) {
//			LOGGER.error("Exception calling processForgotPasswordRequest with email: {} and baseUrl: {}",  forgotPasswordRequest.getEmail(), forgotPasswordRequest.getBaseUrl(), e);
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//		}
//		LOGGER.debug("End ...");
//		return ResponseEntity.ok(null);
//    }
//
//    @PostMapping("/resetPassword")
//	public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
//		LOGGER.debug("Begin ...");
//		LOGGER.debug("resetPasswordRequest: {}", resetPasswordRequest);
//		try {
//			securityService.processResetPasswordRequest(resetPasswordRequest.getResetPasswordJwt(), resetPasswordRequest.getNewPassword(), resetPasswordRequest.getBaseUrl());
//		} catch (ExpiredJwtException e) {
//			LOGGER.warn(e.getMessage());
//			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This reset password link has expired. Please request another password reset.");
//		} catch (SignatureException e) {
//			LOGGER.warn(e.getMessage());
//			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This is not the latest reset password link. Please check your mailbox for the latest reset password email.");
//		} catch (ApplicationException e) {
//			LOGGER.warn(e.getMessage());
//			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//		} catch (Exception e) {
//			LOGGER.error("Exception calling processResetPasswordRequest with resetPasswordJwt: {}, newPassword: ********, baseUrl: {}",  resetPasswordRequest.getResetPasswordJwt(), resetPasswordRequest.getBaseUrl(), e);
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//		}
//		LOGGER.debug("End ...");
//		return ResponseEntity.ok(null);
//    }

}
