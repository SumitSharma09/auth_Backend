package com.sumit.auth_app.dtos;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record APIError(

		int status, String error, String message, String path, OffsetDateTime timestamp

) {

	public static APIError of(int status, String error, String message, String path) {
		return new APIError(status, error, message, path, OffsetDateTime.now(ZoneOffset.UTC));

	}
	public static APIError of(int status, String error, String message, String path, boolean dateTime) {
		return new APIError(status, error, message, path, null);

	}

}
