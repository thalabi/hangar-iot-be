package com.kerneldc.hangariot.security;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JwtUtil {

	private static final String CUSTOM_USER_DETAILS = "customUserDetails";

    private int jwtExpiryInMinutes;
    private int resetPasswordJwtExpiryInMinutes;
	
	private SecretKeyProvider secretKeyProvider;

	private Key secretKey;

	public JwtUtil(
			@Value("${application.security.jwt.token.jwtExpirynMinutes:600}" /* default of 10 hours */) int jwtExpiryInMinutes,
			@Value("${application.security.jwt.token.resetPasswordJwtExpiryInMinutes:60}" /* default of 1 hour */) int resetPasswordJwtExpiryInMinutes,
			SecretKeyProvider secretKeyProvider) {
		this.jwtExpiryInMinutes = jwtExpiryInMinutes;
		this.resetPasswordJwtExpiryInMinutes = resetPasswordJwtExpiryInMinutes;
		this.secretKeyProvider = secretKeyProvider;

		secretKey = secretKeyProvider.getAuthenticationJwtKey();
		LOGGER.debug("Using this secret key for generating JWT token. Secret key: {}", secretKey.getEncoded());

		objectMapper = new ObjectMapper();
		// add mix in for SimpleGrantedAuthority as it does not follow the conventions of a POJO
		objectMapper.addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthorityMixIn.class);

	}
	
	
	private ObjectMapper objectMapper;
	
//	@PostConstruct
//	public void init() {
//		//secretKey = secretKeyProvider.getAuthenticationJwtKey();
//		//LOGGER.debug("Using this secret key for generating JWT token. Secret key: {}", secretKey.getEncoded());
//
////		objectMapper = new ObjectMapper();
////		// add mix in for SimpleGrantedAuthority as it does not follow the conventions of a POJO
////		objectMapper.addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthorityMixIn.class);
//		
//	}

	public String generateToken(CustomUserDetails customUserDetails) {
		Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiryInMinutes*60*1000);
        
        Claims claims = Jwts.claims();
        claims.put(CUSTOM_USER_DETAILS, customUserDetails);
        
		return Jwts.builder()
				.setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey)
                .compact();
	}

	public boolean validateToken(String token) {
        try {
        	Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            
//            Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
//            LOGGER.info("token expiration: {}", claims.getExpiration());
            return true;
            
        } catch (MalformedJwtException ex) {
        	LOGGER.error("JWT is inavlid. Token: {}", token);
        } catch (ExpiredJwtException ex) {
        	LOGGER.error("JWT has expired. Token: {}", token);
        } catch (UnsupportedJwtException ex) {
        	LOGGER.error("Unsupported JWT. Token: {}", token);
        } catch (SignatureException  ex) {
        	LOGGER.error("JWT signature validation failed. Token: {}", token);
        }
        return false;
    }
	
	public CustomUserDetails getCustomUserDetailsFromJwt(String token) {
    	Claims claims = Jwts.parserBuilder()
            	.setSigningKey(secretKey).build()
            	.parseClaimsJws(token)
            	.getBody();
    	LOGGER.debug("claims.get(\"{}\"): {}", CUSTOM_USER_DETAILS, claims.get(CUSTOM_USER_DETAILS));
    	return objectMapper.convertValue(claims.get(CUSTOM_USER_DETAILS), CustomUserDetails.class);
    }

	public record JwtAndKey (String jwt, Key key) {}
	/**
	 * Generate the reset password JWT and return the token and the secret key used to sign it
	 * @param username
	 * @return JwtAndKey, a record containing the JWT and the secret key
	 * @throws NoSuchAlgorithmException
	 */
	public JwtAndKey generateResetPasswordJwt(String username) throws NoSuchAlgorithmException {
		Date now = new Date();
        Date expiryDate = new Date(now.getTime() + resetPasswordJwtExpiryInMinutes*60*1000);
        Key userSecretKey = secretKeyProvider.generatePasswordResetJwtKey();
        String jwt = Jwts.builder()
				.setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(userSecretKey)
                .compact();
        return new JwtAndKey(jwt, userSecretKey);
	}

	/**
	 * Convert JWT to unsigned token and try and extract the username
	 * @param resetPasswordJwt
	 * @return username
     * @throws UnsupportedJwtException  if the {@code claimsJwt} argument does not represent an unsigned Claims JWT
     * @throws MalformedJwtException    if the {@code claimsJwt} string is not a valid JWT
     * @throws SignatureException       if the {@code claimsJwt} string is actually a JWS and signature validation
     *                                  fails
     * @throws ExpiredJwtException      if the specified JWT is a Claims JWT and the Claims has an expiration time
     *                                  before the time this method is invoked.
     * @throws IllegalArgumentException if the {@code claimsJwt} string is {@code null} or empty or only whitespace
	 */
	public String getUsernameFromResetPasswordJwt(String resetPasswordJwt) {
    	final int i = resetPasswordJwt.lastIndexOf('.');
        final String unsignedResetPasswordJwt = resetPasswordJwt.substring(0, i+1);
        Claims claims;
		claims = Jwts.parserBuilder().build().parseClaimsJwt(unsignedResetPasswordJwt).getBody();
		LOGGER.debug("claims: {}", claims);
		LOGGER.debug("issued at: {}, exoires at: {}", claims.getIssuedAt(), claims.getExpiration());
		return claims.getSubject();
	}
}
