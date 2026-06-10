package com.sumit.auth_app.services;

import com.sumit.auth_app.dtos.UserDto;

public interface UserService {

	UserDto createUser(UserDto userDto);

	UserDto getUserByEmail(String email);

	UserDto updateUser(UserDto userDto, String userId);

	void deleteUser(String userId);

	UserDto getUserId(String userId);
	
	Iterable<UserDto> getAllUsers();
}
