package com.mentorlink.modules.faculty.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacultyDTO {
    private Long id;
    private String email;
    private String name;
    private String department;
    private String expertise;
}
