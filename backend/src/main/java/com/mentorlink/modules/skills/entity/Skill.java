package com.mentorlink.modules.skills.entity;

import jakarta.persistence.*;
import lombok.*;

/** Named skill tag for users/projects. */
@Entity
@Table(name = "skills")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}
