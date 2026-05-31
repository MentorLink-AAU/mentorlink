package com.mentorlink.modules.users.entity;

import com.mentorlink.modules.faculty.entity.FacultyProfile;
import com.mentorlink.modules.students.entity.StudentProfile;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
/** Core user entity: credentials, profile fields, roles, student/faculty profile links. */
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @Column(length = 1000)
    private String bio;

    @ElementCollection
    @CollectionTable(name = "user_interests", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "interest")
    @Builder.Default
    private Set<String> interests = new HashSet<>();

    @Column(nullable = false)
    @JsonIgnore   // 🔑 don’t expose password in API responses
    private String password;

    /** True when user was given a default password (e.g. bulk upload). Must change password on first login. */
    @Column(name = "is_first_login", nullable = false)
    @Builder.Default
    private boolean firstLogin = true;

    public boolean isFirstLogin() { return firstLogin; }

    // ✅ Roles
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Builder.Default
    private Set<String> roles = new HashSet<>();

    // ✅ Skills
    @ElementCollection
    @CollectionTable(name = "user_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill")
    @Builder.Default
    private Set<String> skills = new HashSet<>();

    // ✅ Achievements
    @ElementCollection
    @CollectionTable(name = "user_achievements", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "achievement")
    @Builder.Default
    private Set<String> achievements = new HashSet<>();

    // ✅ Student Profile
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore   // 🔑 prevent recursion
    private StudentProfile studentProfile;

    // ✅ Faculty Profile
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore   // 🔑 prevent recursion
    private FacultyProfile facultyProfile;

    // Explicit getter so that code using getRoles() compiles even if Lombok processing is unavailable
    public Set<String> getRoles() {
        return roles;
    }
}
