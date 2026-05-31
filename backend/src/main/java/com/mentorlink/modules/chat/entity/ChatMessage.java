package com.mentorlink.modules.chat.entity;

import com.mentorlink.modules.groups.entity.Group;
import com.mentorlink.modules.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

/** Chat message in a group: sender, content, linked to group. */
@Entity
@Table(name = "chat_messages")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}
