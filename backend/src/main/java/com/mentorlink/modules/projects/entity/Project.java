package com.mentorlink.modules.projects.entity;

import com.mentorlink.modules.faculty.entity.FacultyProfile;
import com.mentorlink.modules.groups.entity.Group;
import jakarta.persistence.*;
import lombok.*;

/** Project: title, description, domain, mentor, linked to one group. */
@Entity
@Table(name = "projects")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String domain;
    private String techStack;

    @Builder.Default
    private int progress = 0; // 0–100%

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    private FacultyProfile mentor;
}
