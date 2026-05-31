// src/main/java/com/mentorlink/modules/auth/dto/RegisterStudentRequest.java
package com.mentorlink.modules.auth.dto;

import lombok.Data;

@Data
public class RegisterStudentRequest {
    private String email;
    private String fullName;
    private String password;
    private String role; // should be "STUDENT"
    private String rollNumber;
    private String department;
    private Integer yearOfStudy;
}
