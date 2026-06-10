package com.sumit.auth_app.controllers;

import java.util.Iterator;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sumit.auth_app.dtos.UserDto;
import com.sumit.auth_app.services.UserService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping
	public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDto));

	}

	@GetMapping
	public ResponseEntity<Iterable<UserDto>> getAllUsers() {
		return ResponseEntity.ok(userService.getAllUsers());

	}
	@GetMapping("/email/{email}")
	public ResponseEntity<UserDto> getUserByEmail(@PathVariable("email") String email){
		
		return ResponseEntity.ok(userService.getUserByEmail(email));
		
	}
	
	@DeleteMapping("/{userId}")
	public void deleteUser(@PathVariable("userId") String userId) {
	userService.deleteUser(userId);	
	}
	
	@PutMapping("/{userId}")
	public ResponseEntity<UserDto> updatedUser(@RequestBody UserDto userDto , @PathVariable("userId") String userId){
		return ResponseEntity.ok(userService.updateUser(userDto, userId));
	}
	
	@GetMapping("/{userId}")
	public ResponseEntity<UserDto> getUserById(@PathVariable("userId") String userId){
		
		return ResponseEntity.ok(userService.getUserId(userId));
		
	}
	

}
