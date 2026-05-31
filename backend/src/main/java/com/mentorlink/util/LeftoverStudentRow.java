package com.mentorlink.util;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeftoverStudentRow {
    private String email;
    private String rollNumber;
}
