package com.sumit.auth_app.dtos;

public record TokenResponse(

		String accessToken, String refreshToken, long expireIn, String tokenType, UserDto user

) {

	public static TokenResponse of(String accessToken, String refereshToken, long expireIn, UserDto user) {

		return new TokenResponse(accessToken, refereshToken, expireIn, "Bearer ", user);

	}

	 public static TokenResponse bearer(String accessToken, String refreshToken, long expiresIn) {
	        return new TokenResponse(accessToken, refreshToken, expiresIn, "Bearer",null);
	    }
	    public static TokenResponse bearerWithUser(String accessToken, String refreshToken, long expiresIn, UserDto user) {
	        return new TokenResponse(accessToken, refreshToken, expiresIn, "Bearer", user);
	    }

}
