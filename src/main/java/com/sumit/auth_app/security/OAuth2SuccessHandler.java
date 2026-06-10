package com.sumit.auth_app.security;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.google.api.client.util.Value;
import com.sumit.auth_app.entities.Provider;
import com.sumit.auth_app.entities.RefreshToken;
import com.sumit.auth_app.entities.User;
import com.sumit.auth_app.repo.RefershTokenRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final com.sumit.auth_app.repo.userRepo userRepo;

	private final com.sumit.auth_app.security.JwtService jwtService;

	private final com.sumit.auth_app.security.CookieService cookieService;

	private final RefershTokenRepository refreshTokenRepository;

	@Value("${app.auth.frontened.success-redirect}")
	private String frontEndSuccessUrl;

	@Override
	@Transactional
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		// TODO Auto-generated method stub
		logger.info("Successful authentication");
		logger.info(authentication.toString());

		OAuth2User OAuth2user = (OAuth2User) authentication.getPrincipal();

		// identify user

		// username
		// user email
		// new usercreate
		String registrationId = "unknown";
		if (authentication instanceof OAuth2AuthenticationToken token) {
			registrationId = token.getAuthorizedClientRegistrationId();
		}

		logger.info("registration Id: " + registrationId);
		logger.info("OAuth2user: " + OAuth2user.getAttributes().toString());

		User user;
		switch (registrationId) {
		case "google" -> {
			String googleId = OAuth2user.getAttributes().getOrDefault("sub", "").toString();
			String email = OAuth2user.getAttributes().getOrDefault("email", "").toString();
			String name = OAuth2user.getAttributes().getOrDefault("name", "").toString();
			String picture = OAuth2user.getAttributes().getOrDefault("picture", "").toString();
			User newUser = User.builder().email(email).name(name).image(picture).enable(true).provider(Provider.GOOGLE)
					.providerId(googleId).build();

//			userRepo.findByEmail(email).ifPresentOrElse(user1 -> {
//				logger.info("user is there in database");
//				logger.info(user1.toString());
//			}, () -> {
//				userRepo.save(user);
//			});
			user = userRepo.findByEmail(email).orElseGet(() -> userRepo.save(newUser));

		}

		case "github" -> {
			String name = OAuth2user.getAttributes().getOrDefault("login", "").toString();
			String githubId = OAuth2user.getAttributes().getOrDefault("id", "").toString();
			String image = OAuth2user.getAttributes().getOrDefault("avtar_url", "").toString();

			String email = (String) OAuth2user.getAttributes().get("email");
			if (email == null) {
				email = name + "@github.com";
			}
			User newUser = User.builder().email(email).name(name).image(image).enable(true).provider(Provider.GITHUB)
					.providerId(githubId).build();

//			userRepo.findByEmail(email).ifPresentOrElse(user1 -> {
//				logger.info("user is there in database");
//				logger.info(user1.toString());
//			}, () -> {
//				userRepo.save(user);
//			});
			user = userRepo.findByEmail(email).orElseGet(() -> userRepo.save(newUser));

		}
		default -> {
			throw new RuntimeException("Invalid Provider");
		}

		}

		String jti = UUID.randomUUID().toString();
		RefreshToken refreshTokenOb = RefreshToken.builder().jti(jti).user(user).revoked(false).createdAt(Instant.now())
				.expireAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds())).build();

		// jwt token_ ke front --- per redirect
		refreshTokenRepository.save(refreshTokenOb);

		String accessToken = jwtService.generateAccessToken(user);
		String refreshToken = jwtService.generateRefreshToken(user, refreshTokenOb.getJti());

		cookieService.attachRefreshCookie(response, refreshToken, (int) jwtService.getRefreshTtlSeconds());

		// response.getWriter().write("Login successful");
		response.sendRedirect(frontEndSuccessUrl);

	}

}
