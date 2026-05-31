package com.mentorlink.modules.notifications.service;

import com.mentorlink.modules.notifications.dto.NotificationDto;
import com.mentorlink.modules.notifications.entity.Notification;
import com.mentorlink.modules.notifications.repository.NotificationRepository;
import com.mentorlink.modules.users.entity.User;
import com.mentorlink.modules.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public Notification create(String userEmail, String message) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Notification n = notificationRepository.save(Notification.builder()
                .user(user)
                .message(message)
                .read(false)
                .build());
        messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), NotificationDto.from(n));
        return n;
    }

    public List<Notification> getForUser(String email, int limit) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.findByUserOrderByIdDesc(user, PageRequest.of(0, limit));
    }

    public long getUnreadCount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.countByUserAndReadFalse(user);
    }

    public Notification markAsRead(Long id, String email) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!n.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }
        n.setRead(true);
        return notificationRepository.save(n);
    }
}
