package com.mentorlink.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** DTO for user creation/update with validation. */
public class UserDTO {

    private Long id;

    @NotBlank(message = "Full name must not be blank")
    private String fullName;

    @Email(message = "Must be a valid email")
    @NotBlank(message = "Email is required")
    private String email;

@NotBlank(message = "Password must not be blank")
private String password;

// Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
