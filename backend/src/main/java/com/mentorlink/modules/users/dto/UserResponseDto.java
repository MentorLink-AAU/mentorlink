// src/main/java/com/mentorlink/modules/users/dto/UserResponseDto.java
package com.mentorlink.modules.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String email;
    private String fullName;
    private String role;
    // student fields (nullable when not student)
    private String rollNumber;
    private String department;
    private Integer yearOfStudy;
    // faculty fields (nullable when not faculty)
    private String expertise;
    private Integer maxGroups;
    private Integer currentLoad;
    // common
    private List<String> skills;
    private List<String> achievements;
    /** True when user must change default password (first login after bulk/auto registration). */
    private boolean requiresPasswordChange;
}
