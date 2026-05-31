package com.mentorlink.modules.users.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "roles", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String name; // STUDENT | FACULTY | ADMIN
}
