package com.kerneldc.hangariot.springconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.kerneldc.hangariot.security.JwtAuthenticationFilter;
import com.kerneldc.hangariot.security.service.CustomUserDetailsService;

import lombok.extern.slf4j.Slf4j;

@EnableWebSecurity
@Slf4j
public class WebSecurityConfigOld /*extends WebSecurityConfigurerAdapter*/ {
/*
	
	@Value("${application.security.disableSecurity:false}")
	private boolean disableSecurity;

    
	@Autowired
	private CustomUserDetailsService customUserDetailsService;
	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	@Autowired
	private UnauthorizedHandler unauthorizedHandler;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
	
	/**
	 * Configure AuthenticationManager to use our CustomUserDetailsService and PasswordEncoder
	 */
	@Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
    }

	@Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
		httpSecurity
			.cors()
				.and()
	    	.csrf().disable()
	    	.exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
	    		.and()
	        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		if (disableSecurity) {
			LOGGER.warn("*** appliction security is currently disabled ***");
			LOGGER.warn("*** to enable set application.security.disableSecurity to false ***");
			httpSecurity.authorizeRequests().anyRequest().permitAll();
			return;
		}
		
		httpSecurity.authorizeRequests()
				.mvcMatchers("/appInfoController/*", "/securityController/authenticate",
						"/webSocketStatsConfigController/changeLoggingPeriod",
						
						// TODO temporarily disable security on Websocket
						"/hangar-iot-websocket*",
						
						/*"/data-rest/*",*/ "/actuator/*").permitAll()
				.anyRequest().authenticated();

		// Add our jwtAuthenticationFilter
		httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

	}
	*/
}
