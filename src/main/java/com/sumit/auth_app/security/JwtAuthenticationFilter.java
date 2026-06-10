package com.sumit.auth_app.security;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sumit.auth_app.helpers.UserHelper;
import com.sumit.auth_app.repo.userRepo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	private final userRepo userrepository;

	private Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
	
	

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		String header = request.getHeader("Authorization");

		logger.info("Authorization header : {}", header);

		if (header != null && header.startsWith("Bearer ")) {

			// token extract and validate then authentication create and then security
			// context ke andar set karunga
			String token = header.substring(7);
				
			try {

				if (!jwtService.isAccessToken(token)) {
					filterChain.doFilter(request, response);
					return;
				}

				Jws<Claims> parse = jwtService.parse(token);

				Claims payload = parse.getPayload();

				String userId = payload.getSubject();

				UUID userUUID = UserHelper.parseUUID(userId);

				userrepository.findById(userUUID).ifPresent(user -> {

					if (user.isEnable()) {
						List<GrantedAuthority> authorities = user.getRoles() == null ? List.of()
								: user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName()))
										.collect(Collectors.toList());
						UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
								user.getEmail(), null, authorities);

						authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						if (SecurityContextHolder.getContext().getAuthentication() == null)
							SecurityContextHolder.getContext().setAuthentication(authentication);
					}
				});

			} catch (ExpiredJwtException e) {
				request.setAttribute("error", "TOKEN EXPIRED");
				
			} catch (Exception e) {
				request.setAttribute("error", "INVALID EXPIRED");
				
			}

		}
		filterChain.doFilter(request, response);
	}
	
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException{
		return request.getRequestURI().startsWith("/api/v1/auth");
		
	}

}
