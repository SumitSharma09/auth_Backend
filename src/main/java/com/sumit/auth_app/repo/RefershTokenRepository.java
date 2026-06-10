package com.sumit.auth_app.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sumit.auth_app.entities.RefreshToken;

public interface RefershTokenRepository extends JpaRepository<RefreshToken, UUID> {

	Optional<RefreshToken> findByJti(String jti);

}
