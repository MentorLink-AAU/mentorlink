package com.mentorlink.modules.faculty.entity;

import com.mentorlink.modules.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

/** Faculty profile: department, expertise, mentor capacity (currentLoad/maxGroups). */
@Entity
@Table(name = "faculty_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String department;
    private String expertise;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(length = 1000)
    private String bio;

    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @Builder.Default
    private int currentLoad = 0;

    @Builder.Default
    private int maxGroups = 3;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
