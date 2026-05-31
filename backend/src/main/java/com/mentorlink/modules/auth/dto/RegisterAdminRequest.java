// src/main/java/com/mentorlink/modules/auth/dto/RegisterAdminRequest.java
package com.mentorlink.modules.auth.dto;

import lombok.Data;

@Data
public class RegisterAdminRequest {
    private String email;
    private String fullName;
    private String password;
    private String role; // "ADMIN"
}
