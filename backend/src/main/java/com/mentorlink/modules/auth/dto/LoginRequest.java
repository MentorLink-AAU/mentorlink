// src/main/java/com/mentorlink/modules/auth/dto/LoginRequest.java
package com.mentorlink.modules.auth.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
