// src/main/java/com/mentorlink/modules/auth/dto/RegisterFacultyRequest.java
package com.mentorlink.modules.auth.dto;

import lombok.Data;

@Data
public class RegisterFacultyRequest {
    private String email;
    private String fullName;
    private String password;
    private String role; // "FACULTY"
    private String department;
    private String expertise;
    private Integer maxGroups;
}
