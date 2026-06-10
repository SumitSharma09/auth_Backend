package com.sumit.auth_app.services;

import com.sumit.auth_app.dtos.UserDto;

public interface AuthService {
	
	UserDto registerUser(UserDto userDto);

}
