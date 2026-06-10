package com.sumit.auth_app.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sumit.auth_app.dtos.APIError;
import com.sumit.auth_app.security.JwtAuthenticationFilter;
import com.sumit.auth_app.security.OAuth2SuccessHandler;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

	
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	
	
	private AuthenticationSuccessHandler successHandler;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(AbstractHttpConfigurer::disable).cors(Customizer.withDefaults())
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
						
						.requestMatchers(AppConstants.AUTH_PUBLIC_URLS).permitAll()
						.anyRequest().authenticated())
				.oauth2Login(oauth2 ->
				        oauth2.successHandler(successHandler)
				        .failureHandler(null)	
						).logout(AbstractHttpConfigurer::disable)
				.exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, e) -> {
					// Log the exception for debugging
					e.printStackTrace();
					response.setStatus(401); // Unauthorized
					response.setContentType("application/json"); // Corrected content type
					// Optionally, you can write a custom error message here
					String message = e.getMessage();
					String error = (String) request.getAttribute("error");
					if(error != null) {
						message = error;
					}
				//	Map<String, String> errorMap = Map.of("mesaage", message, "statusCode", Integer.toString(401));
					var apiError = APIError.of(HttpStatus.UNPROCESSABLE_ENTITY.value(), "UNAUTHORIZED Access", message, request.getRequestURI(), true);
					var objectMapper = new ObjectMapper();
					response.getWriter().write(objectMapper.writeValueAsString(apiError));
				})).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	/*
	 * @Bean public UserDetailsService users() {
	 * 
	 * User.UserBuilder userBuilder = User.withDefaultPasswordEncoder();
	 * 
	 * UserDetails user1 =
	 * userBuilder.username("Sumit").password("abc").roles("ADMIN").build();
	 * UserDetails user2 =
	 * userBuilder.username("Ankit").password("abc11").roles("ADMIN").build();
	 * UserDetails user3 =
	 * userBuilder.username("Rahul").password("abc123").roles("USER").build();
	 * 
	 * return new InMemoryUserDetailsManager(user1, user2, user3);
	 * 
	 * }
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();

	}
}
