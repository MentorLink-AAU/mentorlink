package com.mentorlink.modules.notifications.entity;

import com.mentorlink.modules.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

/** User notification: message and read status. */
@Entity
@Table(name = "notifications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String message;

    @Column(name = "is_read")
    private boolean read = false;
}
