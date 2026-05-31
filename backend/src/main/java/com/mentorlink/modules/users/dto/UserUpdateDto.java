// src/main/java/com/mentorlink/modules/users/dto/UserUpdateDto.java
package com.mentorlink.modules.users.dto;

import lombok.Data;

@Data
public class UserUpdateDto {
    private String fullName;
    private String password;
    private java.util.List<String> skills; // languages, tools, frameworks, databases
}
