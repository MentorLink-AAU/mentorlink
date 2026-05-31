package com.mentorlink.modules.auth.controller;

import com.mentorlink.common.dto.ApiResponse;
import com.mentorlink.common.exception.ApiException;
import com.mentorlink.modules.auth.dto.*;
import com.mentorlink.modules.auth.service.AuthService;
import com.mentorlink.modules.auth.service.PasswordResetService;
import com.mentorlink.modules.users.UserRepository;
import com.mentorlink.modules.users.dto.UserResponseDto;
import com.mentorlink.modules.users.dto.UserUpdateDto;
import com.mentorlink.modules.users.entity.User;
import com.mentorlink.modules.users.service.UserService;
import com.mentorlink.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for authentication: registration (student/faculty/admin),
 * login (generic and role-specific), and profile operations.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /** Register a new student user and student profile. */
    @PostMapping("/register/student")
    public ResponseEntity<ApiResponse<UserResponseDto>> registerStudent(@RequestBody RegisterStudentRequest request) {
        return ResponseEntity.ok(authService.registerStudent(request));
    }

    /** Register a new faculty user and faculty profile. */
    @PostMapping("/register/faculty")
    public ResponseEntity<ApiResponse<UserResponseDto>> registerFaculty(@RequestBody RegisterFacultyRequest request) {
        return ResponseEntity.ok(authService.registerFaculty(request));
    }

    /** Register a new admin user. */
    @PostMapping("/register/admin")
    public ResponseEntity<ApiResponse<UserResponseDto>> registerAdmin(@RequestBody RegisterAdminRequest request) {
        return ResponseEntity.ok(authService.registerAdmin(request));
    }

    /** Authenticate user and return JWT plus first-login flag (requires password change if true). */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = performLogin(loginRequest.getEmail(), loginRequest.getPassword(), null);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** Login restricted to users with ROLE_STUDENT. */
    @PostMapping("/login/student")
    public ResponseEntity<ApiResponse<LoginResponse>> loginStudent(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(ApiResponse.success(performLogin(req.getEmail(), req.getPassword(), "ROLE_STUDENT")));
    }

    /** Login restricted to users with ROLE_FACULTY. */
    @PostMapping("/login/faculty")
    public ResponseEntity<ApiResponse<LoginResponse>> loginFaculty(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(ApiResponse.success(performLogin(req.getEmail(), req.getPassword(), "ROLE_FACULTY")));
    }

    /** Login restricted to users with ROLE_ADMIN. */
    @PostMapping("/login/admin")
    public ResponseEntity<ApiResponse<LoginResponse>> loginAdmin(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(ApiResponse.success(performLogin(req.getEmail(), req.getPassword(), "ROLE_ADMIN")));
    }

    private LoginResponse performLogin(String email, String password, String requiredRole) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        List<String> roles = auth.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .toList();

        if (requiredRole != null && !roles.contains(requiredRole)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "INVALID_ROLE",
                    "This account cannot sign in with the selected role");
        }

        String token = jwtTokenProvider.generate(auth.getName(), roles);
        boolean requiresPasswordChange = userRepository.findByEmail(email)
                .map(User::isFirstLogin)
                .orElse(false);

        return LoginResponse.builder()
                .token(token)
                .requiresPasswordChange(requiresPasswordChange)
                .build();
    }

    /** Request password reset email (generic response; anti-enumeration). */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @RequestBody @jakarta.validation.Valid ForgotPasswordRequest request) {
        String message = passwordResetService.requestReset(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /** Complete password reset using token from email link. */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestBody @jakarta.validation.Valid ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /** Change password (required on first login when user had default password). */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(Authentication authentication,
                                                            @RequestBody @jakarta.validation.Valid ChangePasswordRequest request) {
        authService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /** Return the currently authenticated user's profile. */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> me(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(userService.getCurrentUser(authentication)));
    }

    /** Update the authenticated user's profile. */
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<UserResponseDto>> update(Authentication authentication,
                                                               @RequestBody UserUpdateDto update) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUser(authentication, update)));
    }
}
