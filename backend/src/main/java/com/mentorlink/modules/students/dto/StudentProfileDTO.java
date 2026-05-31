// src/main/java/com/mentorlink/modules/students/dto/StudentProfileDTO.java
package com.mentorlink.modules.students.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentProfileDTO {
    private Long id;
    private Long userId;
    private String rollNumber;
    private String department;
    private Integer yearOfStudy;
}
