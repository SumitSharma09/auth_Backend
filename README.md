# Auth App – Spring Boot 3 Authentication Backend (JWT + OAuth2)

Secure authentication and user-management REST API built with **Java 17** and **Spring Boot 3.5**. It provides stateless **JWT** login with **refresh tokens**, **Google and GitHub social login (OAuth2)**, role-based access control and a React frontend.

## Features

- **JWT authentication** – login, register, logout and access-token refresh (`JwtService`, `JwtAuthenticationFilter`)
- **Refresh tokens** stored in the database and delivered through HTTP-only cookies (`CookieService`)
- **OAuth2 social login** with Google and GitHub via Spring Security OAuth2 Client and a custom `OAuth2SuccessHandler`
- **Role-based access control** with Spring Security and a custom `UserDetailsService`
- **User management CRUD** REST APIs with DTOs (ModelMapper) and Bean Validation
- **Global exception handling** with consistent error responses
- **API documentation** with Springdoc OpenAPI / Swagger UI

## Tech stack

Java 17 · Spring Boot 3.5 · Spring Security · OAuth2 Client · JWT (JJWT) · Spring Data JPA · Hibernate · MySQL · Lombok · ModelMapper · Springdoc OpenAPI · Maven · React

## REST API

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/auth/register` | Register a new user |
| POST | `/api/v1/auth/login` | Log in and receive access + refresh tokens |
| POST | `/api/v1/auth/refresh` | Get a new access token using the refresh token |
| POST | `/api/v1/auth/logout` | Log out and invalidate the refresh token |
| POST | `/api/v1/users` | Create a user |
| GET | `/api/v1/users` | List users |
| GET | `/api/v1/users/{userId}` | Get a user by id |
| GET | `/api/v1/users/email/{email}` | Get a user by email |
| PUT | `/api/v1/users/{userId}` | Update a user |
| DELETE | `/api/v1/users/{userId}` | Delete a user |

## Project structure

```
src/main/java/com/sumit/auth_app
├── config        # Security, OpenAPI and app configuration
├── controllers   # AuthController, UserController
├── dtos          # Request/response DTOs and error models
├── entities      # User, Role, RefreshToken, Provider
├── exceptions    # Global exception handling
├── repo          # Spring Data JPA repositories
├── security      # JWT filter, JWT service, cookies, OAuth2 success handler
└── services      # Auth and user services
```

## Getting started

1. Create a MySQL database named `auth_app`.
2. Set the database credentials, JWT secret and OAuth2 client ids/secrets as environment variables (see `application-dev.properties`).
3. Run:

```bash
./mvnw spring-boot:run
```

The API starts on port **8054**. Swagger UI is available at `/swagger-ui/index.html`.

## Author

**Sumit Sharma** – Java Full Stack Developer (Spring Boot, Microservices, Spring AI)
[LinkedIn](https://www.linkedin.com/in/sumit-sharma-325a24206)
