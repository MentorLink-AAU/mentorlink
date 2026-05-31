package com.mentorlink.modules.dashboard.controller;

import com.mentorlink.common.dto.ApiResponse;
import com.mentorlink.modules.dashboard.dto.*;
import com.mentorlink.modules.dashboard.service.ProfileService;
import com.mentorlink.util.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/** Role-specific profile: get/update and profile photo upload. */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final FileStorageService fileStorageService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getMyProfile(Authentication auth) {
        String email = auth.getName();
        if (auth.getAuthorities().stream().anyMatch(a -> "ROLE_STUDENT".equals(a.getAuthority()))) {
            return ResponseEntity.ok(ApiResponse.success(profileService.getStudentProfile(email)));
        }
        if (auth.getAuthorities().stream().anyMatch(a -> "ROLE_FACULTY".equals(a.getAuthority()))) {
            return ResponseEntity.ok(ApiResponse.success(profileService.getFacultyProfile(email)));
        }
        if (auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            return ResponseEntity.ok(ApiResponse.success(profileService.getAdminProfile(email)));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(new com.mentorlink.common.exception.ApiError("UNKNOWN_ROLE", "Unknown role", "")));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<?>> updateMyProfile(Authentication auth, @RequestBody ProfileUpdateDto dto) {
        String email = auth.getName();
        if (auth.getAuthorities().stream().anyMatch(a -> "ROLE_STUDENT".equals(a.getAuthority()))) {
            return ResponseEntity.ok(ApiResponse.success(profileService.updateStudentProfile(email, dto)));
        }
        if (auth.getAuthorities().stream().anyMatch(a -> "ROLE_FACULTY".equals(a.getAuthority()))) {
            return ResponseEntity.ok(ApiResponse.success(profileService.updateFacultyProfile(email, dto)));
        }
        if (auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            return ResponseEntity.ok(ApiResponse.success(profileService.updateAdminProfile(email, dto)));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(new com.mentorlink.common.exception.ApiError("UNKNOWN_ROLE", "Unknown role", "")));
    }

    @PostMapping("/me/photo")
    public ResponseEntity<ApiResponse<String>> uploadProfilePhoto(Authentication auth,
                                                                  @RequestParam("file") MultipartFile file) throws IOException {
        String email = auth.getName();
        String path = fileStorageService.store(file, "profile-photos");
        String url = "/api/files/" + path;
        profileService.setProfilePictureUrl(email, url);
        return ResponseEntity.ok(ApiResponse.success(url));
    }
}
