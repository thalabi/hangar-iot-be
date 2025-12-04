package com.kerneldc.hangariot.security;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil jwtUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		LOGGER.debug("Begin ...");
		var urlAndQueryString = request.getRequestURL() + (StringUtils.isNoneEmpty(request.getQueryString()) ? "?" + request.getQueryString() : StringUtils.EMPTY);
		LOGGER.debug("url: {}", urlAndQueryString);
		String token = getTokenFromRequest(request);
		LOGGER.debug("token: {}", token);
		if (StringUtils.isNotEmpty(token) && jwtUtil.validateToken(token)) {
			
			CustomUserDetails customUserDetails = jwtUtil.getCustomUserDetailsFromJwt(token);
			LOGGER.debug("customUserDetails: {}", customUserDetails);
			// The constructor of UsernamePasswordAuthenticationToken sets authenticated to true
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
					new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
			usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            
            LOGGER.debug("isAuthenticated: {}", SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
		} else {
			LOGGER.debug("token not present or invalid");
			LOGGER.debug("isAuthenticated: false");
		}
		LOGGER.debug("End ...");
        filterChain.doFilter(request, response);
	}

	private String getTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(SecurityConstants.AUTH_HEADER_NAME);
        LOGGER.debug("authorizationHeader: {}", authorizationHeader);
        String prefix = SecurityConstants.AUTH_HEADER_SCHEMA+StringUtils.SPACE;
        if (StringUtils.isNotEmpty(authorizationHeader) && authorizationHeader.startsWith(prefix)) {
            return authorizationHeader.substring(prefix.length());
        } else {
        	return null;
        }
    }
}
