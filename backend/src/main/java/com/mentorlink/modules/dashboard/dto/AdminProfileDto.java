package com.mentorlink.modules.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminProfileDto {
    private Long userId;
    private String fullName;
    private String email;
    private String contactNumber;
    private String role;
    private String profilePictureUrl;
}
