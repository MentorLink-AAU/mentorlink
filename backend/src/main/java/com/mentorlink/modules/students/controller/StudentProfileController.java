// src/main/java/com/mentorlink/modules/students/controller/StudentProfileController.java
package com.mentorlink.modules.students.controller;

import com.mentorlink.modules.students.dto.StudentProfileDTO;
import com.mentorlink.modules.students.service.StudentProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    @PostMapping("/profile")
    public ResponseEntity<StudentProfileDTO> createOrUpdateProfile(@RequestBody StudentProfileDTO dto) {
        return ResponseEntity.ok(studentProfileService.saveProfile(dto));
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<StudentProfileDTO> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(studentProfileService.getProfile(userId));
    }
}
