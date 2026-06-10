package com.sumit.auth_app.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sumit.auth_app.entities.User;

public interface userRepo extends JpaRepository<User, UUID> {
	
	Optional<User> findByEmail(String email);
	boolean existsByEmail(String email);

}
