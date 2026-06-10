package com.sumit.auth_app.controllers;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sumit.auth_app.dtos.LoginRequest;
import com.sumit.auth_app.dtos.RefreshTokenRequest;
import com.sumit.auth_app.dtos.TokenResponse;
import com.sumit.auth_app.dtos.UserDto;
import com.sumit.auth_app.entities.RefreshToken;
import com.sumit.auth_app.entities.User;
import com.sumit.auth_app.repo.RefershTokenRepository;
import com.sumit.auth_app.repo.userRepo;
import com.sumit.auth_app.security.CookieService;
import com.sumit.auth_app.security.JwtService;
import com.sumit.auth_app.services.AuthService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

	private final AuthService authService;

	private final userRepo userRepository;

	private final AuthenticationManager authenticationManager;

	private final JwtService jwtService;

	private final ModelMapper mapper;

	private final RefershTokenRepository rtA;

	private final CookieService cookieService;

	private final Logger logger = LoggerFactory.getLogger(AuthController.class);

	@PostMapping("/login")
	public ResponseEntity<TokenResponse> login(

			@RequestBody LoginRequest loginRequest, HttpServletResponse response

	) {
		// authenticate

		Authentication authenticate = authenticate(loginRequest);
		User user = userRepository.findByEmail(loginRequest.email())
				.orElseThrow(() -> new BadCredentialsException("Invalid username and Pasword"));

		if (!user.isEnable()) {
			throw new DisabledException("User is disabled.......!!");
		}

		String jti = UUID.randomUUID().toString();
		var refreshToken1 = RefreshToken.builder().jti(jti).user(user).createdAt(Instant.now())
				.expireAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds())).build();

		// refresh token save -- information
		rtA.save(refreshToken1);

		// access token-generate
		String accessToken = jwtService.generateAccessToken(user);

		String refreshToken = jwtService.generateRefreshToken(user, refreshToken1.getJti());

		// use cookie service to attach refresh token in cookie

		cookieService.attachRefreshCookie(response, refreshToken, (int) jwtService.getRefreshTtlSeconds());
		cookieService.addNoStoreHeaders(response);

		TokenResponse Tokenresponse = TokenResponse.of(accessToken, refreshToken, jwtService.getAccessTtlSeconds(),
				mapper.map(user, UserDto.class));
		return ResponseEntity.ok(Tokenresponse);
	}

	private Authentication authenticate(LoginRequest loginRequest) {
		// TODO Auto-generated method stub
		try {

			return authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));

		} catch (Exception e) {
			throw new BadCredentialsException("Inavlid User Crendentials.....!!!");
		}

	}

	// access and refresh token renew karne k liye api

	@PostMapping("/refresh")
	public ResponseEntity<TokenResponse> refreshToken(@RequestBody(required = false) RefreshTokenRequest body,
			HttpServletResponse response, HttpServletRequest request) {

		String refreshToken = readRefreshTokenFromRequest(body, request)
				.orElseThrow(() -> new BadCredentialsException("Invalid Refresh Token"));

		if (!jwtService.isRefreshToken(refreshToken)) {
			throw new BadCredentialsException("Invalid Refresh Token Type");
		}
		String jti = jwtService.getJti(refreshToken);
		UUID userId = jwtService.getUserId(refreshToken);
		RefreshToken storedRefreshToken = rtA.findByJti(jti)
				.orElseThrow(() -> new BadCredentialsException("Invalid Refresh Token"));

		if (storedRefreshToken.isRevoked()) {
			throw new BadCredentialsException("Refresh token revoked!!");
		}

		if (storedRefreshToken.getExpireAt().isBefore(Instant.now())) {
			throw new BadCredentialsException("Refresh token expired!!");
		}
		if (!storedRefreshToken.getUser().getId().equals(userId)) {
			throw new BadCredentialsException("Refresh token does not belong to this user!!");

		}

		storedRefreshToken.setRevoked(true);
		String newJti = UUID.randomUUID().toString();
		storedRefreshToken.setReplacedByToken(newJti);
		rtA.save(storedRefreshToken);

		User user = storedRefreshToken.getUser();

		var newRefreshTokenOb = RefreshToken.builder().jti(newJti).user(user).createdAt(Instant.now())
				.expireAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds())).revoked(false).build();

		rtA.save(newRefreshTokenOb);

		String newaccessToken = jwtService.generateAccessToken(user);
		String newrefreshToken = jwtService.generateRefreshToken(user, newRefreshTokenOb.getJti());

		cookieService.attachRefreshCookie(response, newrefreshToken, (int) jwtService.getRefreshTtlSeconds());
		cookieService.addNoStoreHeaders(response);

		return ResponseEntity.ok(TokenResponse.of(newaccessToken, newrefreshToken, jwtService.getAccessTtlSeconds(),
				mapper.map(user, UserDto.class)));

	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		readRefreshTokenFromRequest(null, request).ifPresent(token -> {
			try {
				if (jwtService.isRefreshToken(token)) {
					String jti = jwtService.getJti(token); // fixed method reference
					rtA.findByJti(jti).ifPresent(rt -> { // fixed method usage
						rt.setRevoked(true);
						rtA.save(rt);
					});
				}
			} catch (JwtException ignored) {
				// log the exception if needed
			}
		});

		cookieService.clearRefreshCookie(response);
		cookieService.addNoStoreHeaders(response);
		SecurityContextHolder.clearContext();
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	private Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request) {
		// 1) Prefer secure HttpOnly cookie

		if (request.getCookies() != null) {
			Optional<String> fromCookie = Arrays.stream(request.getCookies())
					.filter(c -> cookieService.getRefreshTokenCookieName().equals(c.getName())) // <-- use util name
					.map(c -> c.getValue()).filter(v -> v != null && !v.isBlank()).findFirst();
			if (fromCookie.isPresent()) {
				return fromCookie;
			}
		}

		// 2) Body
		if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
			return Optional.of(body.refreshToken().trim());
		}

		// 3) Custom header
		String refreshHeader = request.getHeader("X-Refresh-Token");
		if (refreshHeader != null && !refreshHeader.isBlank()) {
			return Optional.of(refreshHeader.trim());
		}

		// 4) Authorization: Bearer <token> (only if actually refresh)
		String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
			String candidate = authHeader.substring(7).trim();
			if (!candidate.isEmpty()) {
				try {
					if (jwtService.isRefreshToken(candidate)) {
						return Optional.of(candidate);
					}
				} catch (Exception ignored) {
				}
			}
		}

		return Optional.empty();
	}

	@PostMapping("/register")
	public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto));
	}
}
