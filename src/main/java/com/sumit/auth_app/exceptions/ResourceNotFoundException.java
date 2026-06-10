package com.sumit.auth_app.exceptions;

@SuppressWarnings("serial")
public class ResourceNotFoundException extends RuntimeException{
	
	public ResourceNotFoundException(String message) {
		super(message);
	}
	
	public ResourceNotFoundException() {
		super("Resource not found !!");
	}

}
