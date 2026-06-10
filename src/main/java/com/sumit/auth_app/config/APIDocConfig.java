package com.sumit.auth_app.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Auth Application buid by Sumit Sharma", description = "Generic auth app that can be used with any application", contact = @Contact(name = "Sumit Sharma", url = "", email = "sumit.javadeveloper09@gmail.com"), version = "1.0", summary = "This app is very useful if you don't want create auth app from scratch"

), security = { @SecurityRequirement(name = "bearerAuth") }

)
@SecurityScheme(
		name = "bearerAuth",
		type = SecuritySchemeType.HTTP,
		scheme = "bearer",
		bearerFormat = "JWT"
		
		)
public class APIDocConfig {

}
