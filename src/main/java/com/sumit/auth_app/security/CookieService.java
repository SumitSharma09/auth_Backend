package com.sumit.auth_app.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;

@Service
@Getter
@Setter
public class CookieService {

	private final String refreshTokenCookieName;
	private final boolean cookieHttpOnly;
	private final boolean cookieSecure;
	private final String cookieDomain;
	private final String cookieSameSite;

	private final Logger logger = LoggerFactory.getLogger(CookieService.class);

	public CookieService(@Value("${security.jwt.refresh-token-cookie-name}") String refreshTokenCookieName,
			@Value("${security.jwt.cookie.http-only}") boolean cookieHttpOnly,
			@Value("${security.jwt.cookie-secure}") boolean cookieSecure,
			@Value("${security.jwt.cookie-domain}") String cookieDomain,
			@Value("${security.jwt.cookie-same-site}") String cookieSameSite) {

		this.refreshTokenCookieName = refreshTokenCookieName;
		this.cookieHttpOnly = cookieHttpOnly;
		this.cookieSecure = cookieSecure;
		this.cookieDomain = cookieDomain;
		this.cookieSameSite = cookieSameSite;

	}
	 public String getRefreshTokenCookieName() {
	        return refreshTokenCookieName;
	    }

	// create method to attach cookie to response
	public void attachRefreshCookie(HttpServletResponse response, String value, int maxAgeseconds) {

		logger.info("Attaching cookie with name: {} and value: {}", refreshTokenCookieName, value);

		 ResponseCookie.ResponseCookieBuilder responseCookieBuilder = ResponseCookie.from(refreshTokenCookieName, value)
				 .httpOnly(true)
				.secure(cookieSecure).path("/").maxAge(maxAgeseconds).sameSite(cookieSameSite);

		if (cookieDomain != null && !cookieDomain.isBlank()) {
			responseCookieBuilder.domain(cookieDomain);
		}
		ResponseCookie responseCookie = responseCookieBuilder.build();
		response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
	}

	// clear refresh cookie
	public void clearRefreshCookie(HttpServletResponse response) {
		ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(refreshTokenCookieName, "")
				.httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite);

		if (cookieDomain != null && !cookieDomain.isBlank()) {
			builder.domain(cookieDomain);
		}

		ResponseCookie cookie = builder.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	public void addNoStoreHeaders(HttpServletResponse response) {
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-Store");
		response.setHeader("Pragma", "no-cache");
	}

}
