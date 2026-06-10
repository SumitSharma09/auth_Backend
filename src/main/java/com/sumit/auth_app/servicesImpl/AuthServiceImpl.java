package com.sumit.auth_app.servicesImpl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sumit.auth_app.dtos.UserDto;
import com.sumit.auth_app.services.AuthService;
import com.sumit.auth_app.services.UserService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService{

	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	
	@Override
	public UserDto registerUser(UserDto userDto) {
		// TODO Auto-generated method stub
		
		//Logic
		//Verify email
		//verify password
		//default roles
		userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
		UserDto userDto1 = userService.createUser(userDto);
		return userDto1;
	}

}
