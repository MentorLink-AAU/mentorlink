// AdminRegisterRequest.java
package com.mentorlink.modules.auth.dto;

import lombok.Data;

@Data
public class AdminRegisterRequest {
    private String email;
    private String fullName;
    private String password;
}
