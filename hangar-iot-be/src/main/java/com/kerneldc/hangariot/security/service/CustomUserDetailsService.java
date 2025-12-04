package com.kerneldc.hangariot.security.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.kerneldc.hangariot.security.CustomUserDetails;
import com.kerneldc.hangariot.security.domain.user.User;
import com.kerneldc.hangariot.security.service.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

//@Service
//@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) {
    	LOGGER.info("Begin ...");
    	
//    	initDb();
		List<User> oneUserList = userRepository.findByUsernameAndEnabled(username, true);
		LOGGER.debug("oneUserList.isEmpty(): {}", oneUserList.isEmpty());
		if (oneUserList.isEmpty()) {
			LOGGER.debug("About to throw UsernameNotFoundException");
			throw new UsernameNotFoundException("User not found with username or email : " + username);
		}

		User user = oneUserList.get(0);
		
		//Set<Permission> permissionSet = user.getGroupSet().stream().map(Group::getPermissionSet).flatMap(Set::stream).collect(Collectors.toSet());
        //user.setPermissionSet(permissionSet);
		
		LOGGER.debug("End ...");
		return customUserDetailsFromUser(user);

	}

	private CustomUserDetails customUserDetailsFromUser(User user) {
		CustomUserDetails customUserDetails = new CustomUserDetails();
		customUserDetails.setId(user.getId());
		customUserDetails.setUsername(user.getUsername());
		customUserDetails.setPassword(user.getPassword());
		customUserDetails.setFirstName(user.getFirstName());
		customUserDetails.setLastName(user.getLastName());
		List<GrantedAuthority> authorities = new ArrayList<>();
		user.getPermissionSet().stream().forEach(permission->authorities.add(new SimpleGrantedAuthority(permission.getName())));
		customUserDetails.setAuthorities(authorities);
		return customUserDetails;
	}

}
