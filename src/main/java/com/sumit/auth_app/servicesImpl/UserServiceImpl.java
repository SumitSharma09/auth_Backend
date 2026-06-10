package com.sumit.auth_app.servicesImpl;

import java.time.Instant;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.sumit.auth_app.dtos.UserDto;
import com.sumit.auth_app.entities.Provider;
import com.sumit.auth_app.entities.User;
import com.sumit.auth_app.exceptions.ResourceNotFoundException;
import com.sumit.auth_app.helpers.UserHelper;
import com.sumit.auth_app.repo.userRepo;
import com.sumit.auth_app.services.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final userRepo userRepository;

	private final ModelMapper modelmapper;

	@Override
	@Transactional
	public UserDto createUser(UserDto userDto) {
		// TODO Auto-generated method stub
		if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
			throw new IllegalArgumentException("Email is required");
		}
		if (userRepository.existsByEmail(userDto.getEmail())) {
			throw new IllegalArgumentException("User Email are already exits");
		}

		User user = modelmapper.map(userDto, User.class);
		user.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);
		User savedUser = userRepository.save(user);

		return modelmapper.map(savedUser, UserDto.class);
	}

	@Override
	public UserDto getUserByEmail(String email) {
		// TODO Auto-generated method stub
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with given email id "));
		return modelmapper.map(user, UserDto.class);
	}

	@Override
	public UserDto updateUser(UserDto userDto, String userId) {
		// TODO Auto-generated method stub
		UUID uId = UserHelper.parseUUID(userId);
		User user = userRepository.findById(uId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with given id"));
		
		if(userDto.getName() != null) {
			user.setName(userDto.getName());
		}
		if(userDto.getImage() != null) {
			user.setImage(userDto.getImage());
		}
		if(userDto.getProvider() != null) {
			user.setProvider(userDto.getProvider());
		}
		if(userDto.getPassword() != null) {
			user.setPassword(userDto.getPassword());
		}
		user.setEnable(userDto.isEnable());
		user.setUpdatedAt(Instant.now());
		User updatedUser = userRepository.save(user);
		
		return modelmapper.map(updatedUser, UserDto.class);
	}

	@Override
	public void deleteUser(String userId) {
		// TODO Auto-generated method stub
		UUID uId = UserHelper.parseUUID(userId);
		User user = userRepository.findById(uId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with given id"));
		userRepository.delete(user);
	}

	@Override
	public UserDto getUserId(String userId) {
		// TODO Auto-generated method stub
		User user = userRepository.findById(UserHelper.parseUUID(userId))
				.orElseThrow(() -> new ResourceNotFoundException("User not found with given id"));

		return modelmapper.map(user, UserDto.class);
	}

	@Override
	@Transactional
	public Iterable<UserDto> getAllUsers() {
		// TODO Auto-generated method stub
		return userRepository.findAll().stream().map(user -> modelmapper.map(user, UserDto.class)).toList();
	}

}
