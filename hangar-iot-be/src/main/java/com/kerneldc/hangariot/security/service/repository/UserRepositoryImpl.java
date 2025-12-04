package com.kerneldc.hangariot.security.service.repository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.hangariot.security.domain.user.User;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Trivial implementation of UserRepository that reads data from json file.
 *
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository {

	@Value("classpath:user.json")
	private Resource userJsonResource;
	
	private final ObjectMapper objectMapper;

	private List<User> userList;
	
	@PostConstruct
	public void loadUserData() {
		try {
			userList = objectMapper.readValue(userJsonResource.getInputStream(), new TypeReference<List<User>>(){});
			for (User user: userList) {
				user.setPermissionSet(user.getGroupSet().stream().flatMap(group -> group.getPermissionSet().stream()).collect(Collectors.toSet()));
				LOGGER.info("Loaded User, username: [{}]", user.getUsername());
				LOGGER.info("user: [{}]", user);			}
		} catch (IOException e) {
			LOGGER.error("Unable to read and parse user.json file. Reason: {}", ExceptionUtils.getRootCause(e));
		}
	}

	@Override
	public List<User> findByUsernameAndEnabled(String username, Boolean enabled) {
		return userList.stream().filter(user -> StringUtils.equals(user.getUsername(), username)
				&& BooleanUtils.compare(user.getEnabled(), enabled) == 0).toList();
	}

}
