package com.sumit.auth_app.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sumit.auth_app.entities.User;
import com.sumit.auth_app.exceptions.ResourceNotFoundException;
import com.sumit.auth_app.repo.userRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

	private final userRepo userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		User user = userRepository.findByEmail(username)
				.orElseThrow(() -> new BadCredentialsException("Invalid Email or Password !!"));

		return user;
	}

}
