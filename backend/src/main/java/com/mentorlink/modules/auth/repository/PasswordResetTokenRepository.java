package com.mentorlink.modules.auth.repository;

import com.mentorlink.modules.auth.entity.PasswordResetToken;
import com.mentorlink.modules.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    void deleteAllByUser(User user);

    long countByUserAndCreatedAtAfter(User user, Instant after);
}
