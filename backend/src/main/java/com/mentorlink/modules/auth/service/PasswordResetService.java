package com.mentorlink.modules.auth.service;

import com.mentorlink.common.exception.ApiException;
import com.mentorlink.modules.auth.dto.ResetPasswordRequest;
import com.mentorlink.modules.auth.entity.PasswordResetToken;
import com.mentorlink.modules.auth.repository.PasswordResetTokenRepository;
import com.mentorlink.modules.users.UserRepository;
import com.mentorlink.modules.users.entity.User;
import com.mentorlink.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;

/** Forgot-password request handling, token lifecycle, and password reset. */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private static final int TOKEN_BYTES = 32;
    private static final int EXPIRY_MINUTES = 15;
    private static final int MAX_REQUESTS_PER_HOUR = 3;

    private static final String GENERIC_FORGOT_MESSAGE =
            "If an account exists for this email, you will receive password reset instructions shortly.";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailNotificationService emailNotificationService;

    @Value("${app.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    /**
     * Always returns the same message. Sends email only when user exists and rate limit allows.
     */
    @Transactional
    public String requestReset(String emailRaw) {
        if (emailRaw == null || emailRaw.isBlank()) {
            return GENERIC_FORGOT_MESSAGE;
        }
        String email = emailRaw.trim();
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) {
            log.debug("Password reset requested for unknown email (no email sent)");
            return GENERIC_FORGOT_MESSAGE;
        }
        if (mailUsername == null || mailUsername.isBlank() || mailPassword == null || mailPassword.isBlank()) {
            log.warn("Password reset requested but mail is not configured (MAIL_USERNAME / MAIL_PASSWORD). No email sent.");
            return GENERIC_FORGOT_MESSAGE;
        }
        User user = userOpt.get();
        Instant since = Instant.now().minus(1, ChronoUnit.HOURS);
        if (tokenRepository.countByUserAndCreatedAtAfter(user, since) >= MAX_REQUESTS_PER_HOUR) {
            log.warn("Password reset rate limit hit for user id {}", user.getId());
            return GENERIC_FORGOT_MESSAGE;
        }

        tokenRepository.deleteAllByUser(user);

        String rawToken = generateRawToken();
        String tokenHash = sha256Hex(rawToken);
        Instant now = Instant.now();
        PasswordResetToken entity = PasswordResetToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(now.plus(EXPIRY_MINUTES, ChronoUnit.MINUTES))
                .createdAt(now)
                .build();
        tokenRepository.save(entity);

        String base = frontendBaseUrl.replaceAll("/+$", "");
        String resetLink = base + "/reset-password?token=" + rawToken;
        emailNotificationService.sendPasswordReset(user.getEmail(), resetLink);

        return GENERIC_FORGOT_MESSAGE;
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        if (req.getToken() == null || req.getToken().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "Invalid or expired reset link.");
        }
        String tokenHash = sha256Hex(req.getToken().trim());
        PasswordResetToken row = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "Invalid or expired reset link."));

        if (row.getExpiresAt().isBefore(Instant.now())) {
            tokenRepository.delete(row);
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "Invalid or expired reset link.");
        }

        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PASSWORD_MISMATCH", "New password and confirm password do not match.");
        }
        validatePasswordPolicy(req.getNewPassword());

        User user = row.getUser();
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setFirstLogin(false);
        userRepository.save(user);
        tokenRepository.deleteAllByUser(user);
    }

    private static void validatePasswordPolicy(String password) {
        if (password.length() < 8) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "WEAK_PASSWORD", "New password must be at least 8 characters.");
        }
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "WEAK_PASSWORD",
                    "New password must contain at least one letter and one number.");
        }
    }

    private static String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private static String sha256Hex(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
